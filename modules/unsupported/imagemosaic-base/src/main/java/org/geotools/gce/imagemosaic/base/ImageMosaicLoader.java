/**
 * 
 */
package org.geotools.gce.imagemosaic.base;

import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.util.logging.Level;

import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.ROI;
import javax.media.jai.operator.MosaicDescriptor;

import org.geotools.image.ImageWorker;
import org.geotools.resources.image.ImageUtilities;

import com.vividsolutions.jts.geom.Envelope;

class ImageMosaicLoader
{
    ImageMosaicMetadata metadata;
    ParameterBlockJAI pbjMosaic;
    int numImages;
    ROI[] rois;
    PlanarImage[] alphaChannels;
    Area finalLayout = new Area();

    boolean alphaIn;
    boolean doTransparentColor;
    boolean doInputImageThreshold;
    boolean blend;
    
    double inputImageThresholdValue;
    
    int[] alphaIndex;
    ColorModel model;
    Color transparentColor;
    RenderedImage loadedImage;
    
    
    ImageMosaicLoader(int numImages)
    {
        rois = new ROI[numImages];
        alphaChannels = new PlanarImage[numImages];            
    }
    
    void handleFirstImage()
    {
        // We check here if the images have an alpha channel or some
        // other sort of transparency. In case we have transparency
        // I also save the index of the transparent channel.
        model = loadedImage.getColorModel();
        alphaIn = model.hasAlpha();
        if (alphaIn)
            alphaIndex = new int[] { model.getNumComponents() - 1 };

        // ROI has to be computed depending on the value of the
        // input threshold and on the data type of the images.
        //
        // If I request a threshod of 0 on a byte image, I can skip
        // doing the ROI!
        doInputImageThreshold = checkIfThresholdIsNeeded(
                loadedImage, inputImageThresholdValue);

        // Checking if we have to do something against the final
        // transparent color.
        //
        // If we have a valid transparent color we have to remove
        // the input alpha information.
        //
        // However a possible optimization is to check for index
        // color model images with transparency where the
        // transparent color is the same requested here and no ROIs
        // requested.
        if (transparentColor != null)
        {
            // paranoiac check on the provided transparent color
            transparentColor = new Color(transparentColor.getRed(),
                    transparentColor.getGreen(), transparentColor
                            .getBlue());
            doTransparentColor = true;

            // If the images use an IndexColorModel Bitamsk where
            // the transparent color is the same that was requested,
            // the optimization is to avoid removing the alpha
            // information just to readd it at the end. We can
            // simply go with what we have from the input.
            //
            // However, we have to take into account that no action
            // has to be take if a ROI is requested on the input
            // images since that would imply doing an RGB
            // conversion.
            if (model instanceof IndexColorModel
                    && alphaIn
                    && model.getTransparency() == Transparency.BITMASK)
            {
                final IndexColorModel icm = (IndexColorModel) model;
                final int transparentPixel = icm
                        .getTransparentPixel();
                if (transparentPixel != -1)
                {
                    final int oldTransparentColor = icm
                            .getRGB(transparentPixel);
                    if (oldTransparentColor == transparentColor
                            .getRGB())
                    {
                        doTransparentColor = false;
                    }
                }
            }
        }
    }

    void addToMosaic(ParameterBlockJAI pbjMosaic, Envelope bound,
            Point2D ulc, double[] res, int i)
    {
        // /////////////////////////////////////////////////////////////////////
        //
        // Computing TRANSLATION AND SCALING FACTORS
        //
        // Using the spatial resolution we compute the translation factors for
        // positioning the actual image correctly in final mosaic.
        //
        // /////////////////////////////////////////////////////////////////////
        RenderedImage readyToMosaicImage = scaleAndTranslate(bound, ulc, res,
                loadedImage);

        // ///////////////////////////////////////////////////////////////////
        //
        // INDEX COLOR MODEL EXPANSION
        //
        // Take into account the need for an expansions of the original color
        // model.
        //
        // If the original color model is an index color model an expansion
        // might be requested in case the differemt palettes are not all the
        // same. In this case the mosaic operator from JAI would provide wrong
        // results since it would take the first palette and use that one for
        // all the other images.
        //
        // There is a special case to take into account here. In case the input
        // images use an IndexColorModel t might happen that the transparent
        // color is present in some of them while it is not present in some
        // others. This case is the case where for sure a color expansion is
        // needed. However we have to take into account that during the masking
        // phase the images where the requested transparent color was present
        // willl have 4 bands, the other 3. If we want the mosaic to work we
        // have to add na extra band to the latter type of images for providing
        // alpha information to them.
        //
        //
        // ///////////////////////////////////////////////////////////////////
        if (metadata.getColorModelExpansion()
                && readyToMosaicImage.getColorModel() instanceof IndexColorModel)
        {
            readyToMosaicImage = new ImageWorker(readyToMosaicImage)
                    .forceComponentColorModel().getPlanarImage();
        }

        // ///////////////////////////////////////////////////////////////////
        //
        // TRANSPARENT COLOR MANAGEMENT
        //
        //
        // ///////////////////////////////////////////////////////////////////
        if (doTransparentColor)
        {
            if (ImageMosaicReader.LOGGER.isLoggable(Level.FINE))
                ImageMosaicReader.LOGGER.fine("Support for alpha on input image number " + i);
            // //////////////////////////////////////////////////////////////////
            // ///
            //
            // If requested I can perform the ROI operation on the prepared ROI
            // image for building up the alpha band
            //
            // //////////////////////////////////////////////////////////////////
            // ///
            ImageWorker w = new ImageWorker(readyToMosaicImage);
            if (readyToMosaicImage.getColorModel() instanceof IndexColorModel)
            {
                readyToMosaicImage = w.makeColorTransparent(transparentColor)
                        .getPlanarImage();
            }
            else
                readyToMosaicImage = w.makeColorTransparent(transparentColor)
                        .getPlanarImage();
            alphaIndex = new int[] { readyToMosaicImage.getColorModel()
                    .getNumComponents() - 1 };

        }
        // ///////////////////////////////////////////////////////////////////
        //
        // ROI
        //
        // ///////////////////////////////////////////////////////////////////
        if (doInputImageThreshold)
        {
            ImageWorker w = new ImageWorker(readyToMosaicImage);
            w.tileCacheEnabled(false).intensity().binarize(
                    inputImageThresholdValue);
            rois[i] = w.getImageAsROI();

        }
        else if (alphaIn || doTransparentColor)
        {
            ImageWorker w = new ImageWorker(readyToMosaicImage);
            // //////////////////////////////////////////////////////////////////
            // ///
            //
            // ALPHA in INPUT
            //
            // I have to select the alpha band and provide it to the final
            // mosaic operator. I have to force going to ComponentColorModel in
            // case the image is indexed.
            //
            // //////////////////////////////////////////////////////////////////
            // ///
            if (readyToMosaicImage.getColorModel() instanceof IndexColorModel)
            {
                alphaChannels[i] = w.forceComponentColorModel()
                        .retainLastBand().getPlanarImage();
            }

            else
                alphaChannels[i] = w.retainBands(alphaIndex).getPlanarImage();

        }

        // /////////////////////////////////////////////////////////////////////
        //
        // ADD TO MOSAIC
        //
        // /////////////////////////////////////////////////////////////////////
        pbjMosaic.addSource(readyToMosaicImage);
        PlanarImage pImage = PlanarImage.wrapRenderedImage(readyToMosaicImage);
        Area area = new Area(pImage.getBounds());
        finalLayout.add(area);
    }

    
    void handleAnyImage()
    {
        // Prepare the last parameters for the mosaic.
        //
        // First of all we set the input threshold accordingly to the input
        // image data type. I find the default value (which is 0) very bad
        // for data type other than byte and ushort. With float and double
        // it can cut off a large par of fthe dynamic.
        //
        // Second step is the the management of the input threshold that is
        // converted into a roi because the way we want to manage such
        // threshold is by applying it on the intensitiy of the input image.
        // Note that this ROI has to be mutually exclusive with the alpha
        // management due to the rules of the JAI Mosaic Operation which
        // ignore the ROIs in case an alpha information is provided for the
        // input images.
        //
        // Third step is the management of the alpha information which can
        // be the result of a masking operation upong the request for a
        // transparent color or the result of input images with internal
        // transparency.
        //
        // Fourth step is the blending for having nice Fading effect at
        // overlapping regions.

        double th = getThreshold(loadedImage.getSampleModel().getDataType());
        pbjMosaic
                .setParameter("sourceThreshold", new double[][] { { th } });
        if (doInputImageThreshold)
        {
            // Set the ROI parameter in case it was requested by setting a
            // threshold.
            pbjMosaic.setParameter("sourceROI", rois);

        }
        else if (alphaIn || doTransparentColor)
        {
            // In case the input images have transparency information this
            // way we can handle it.
            pbjMosaic.setParameter("sourceAlpha", alphaChannels);

        }
        // It might important to set the mosaic tpe to blend otherwise
        // sometimes strange results jump in.
        if (blend)
        {
            pbjMosaic.setParameter("mosaicType",
                    MosaicDescriptor.MOSAIC_TYPE_BLEND);

        }            
    }        
    /**
     * ROI has to be computed depending on the value of the input threshold and
     * on the data type of the images.
     * 
     * If I request a threshod of 0 on a byte image, I can skip doing the ROI!
     * 
     * @param loadedImage
     *            to check before applying a threshold.
     * @param thresholdValue
     *            is the value that is suggested to be used for the threshold.
     * @return true in case the threshold is to be performed, false otherwise.
     */
    private boolean checkIfThresholdIsNeeded(RenderedImage loadedImage,
            double thresholdValue)
    {
        if (Double.isNaN(thresholdValue) || Double.isInfinite(thresholdValue))
            return false;
        
        switch (loadedImage.getSampleModel().getDataType())
        {
            case DataBuffer.TYPE_BYTE:
                int bTh = (int) thresholdValue;
                if (bTh <= 0 || bTh >= 255)
                    return false;
                break;
                
            case DataBuffer.TYPE_USHORT:
                int usTh = (int) thresholdValue;
                if (usTh <= 0 || usTh >= 65535)
                    return false;
                break;
                
            case DataBuffer.TYPE_SHORT:
                int sTh = (int) thresholdValue;
                if (sTh <= Short.MIN_VALUE || sTh >= Short.MAX_VALUE)
                    return false;
                break;
                
            case DataBuffer.TYPE_INT:
                int iTh = (int) thresholdValue;
                if (iTh <= Integer.MIN_VALUE || iTh >= Integer.MAX_VALUE)
                    return false;
                break;
                
            case DataBuffer.TYPE_FLOAT:
                float fTh = (float) thresholdValue;
                if (fTh <= -Float.MAX_VALUE || fTh >= Float.MAX_VALUE
                        || Float.isInfinite(fTh) || Float.isNaN(fTh))
                    return false;
                break;
                
            case DataBuffer.TYPE_DOUBLE:
                double dTh = (double) thresholdValue;
                if (dTh <= -Double.MAX_VALUE || dTh >= Double.MAX_VALUE
                        || Double.isInfinite(dTh) || Double.isNaN(dTh))
                    return false;
                break;

        }
        return true;
    }

    /**
     * Returns a suitable threshold depending on the {@link DataBuffer} type.
     * 
     * <p>
     * Remember that the threshold works with >=.
     * 
     * @param dataType
     *            to create a low threshold for.
     * @return a minimum threshold value suitable for this data type.
     */
    private double getThreshold(int dataType)
    {
        switch (dataType)
        {
            case DataBuffer.TYPE_BYTE:
            case DataBuffer.TYPE_USHORT:
                // XXX change to zero when bug fixed
                return 1.0;
                
            case DataBuffer.TYPE_INT:
                return Integer.MIN_VALUE;
            
            case DataBuffer.TYPE_SHORT:
                return Short.MIN_VALUE;
            
            case DataBuffer.TYPE_DOUBLE:
                return -Double.MAX_VALUE;
            
            case DataBuffer.TYPE_FLOAT:
                return -Float.MAX_VALUE;
        }
        return 0;
    }

    /**
     * Computing TRANSLATION AND SCALING FACTORS
     * 
     * Using the spatial resolution we compute the translation factors for
     * positioning the actual image correctly in final mosaic.
     * 
     * @param bound
     * @param ulc
     * @param res
     * @param image
     * @return
     */
    private RenderedImage scaleAndTranslate(Envelope bound, Point2D ulc,
            double[] res, RenderedImage image)
    {
        // evaluate translation and scaling factors.
        double resX = (bound.getMaxX() - bound.getMinX()) / image.getWidth();
        double resY = (bound.getMaxY() - bound.getMinY()) / image.getHeight();
        double scaleX = 1.0, scaleY = 1.0;
        double xTrans = 0.0, yTrans = 0.0;
        if (Math.abs((resX - res[0]) / resX) > ImageMosaicReader.EPS
                || Math.abs(resY - res[1]) > ImageMosaicReader.EPS)
        {
            scaleX = res[0] / resX;
            scaleY = res[1] / resY;

        }
        xTrans = (bound.getMinX() - ulc.getX()) / res[0];
        yTrans = (ulc.getY() - bound.getMaxY()) / res[1];

        // build an image layout that will make the tiles match exactly the
        // transformed image
        ImageLayout layout = new ImageLayout();
        layout.setTileGridXOffset((int) Math.round(xTrans));
        layout.setTileGridYOffset((int) Math.round(yTrans));

        //
        // Optimising scale and translate.
        //
        // In case the scale factors are very close to 1 we have two
        // optimizations: if the translation factors are close to zero we do
        // thing, otherwise if they are integers we do a simple translate.
        //
        // In the general case when we have translation and scaling we do a
        // warp affine which is the most precise operation we can perform.
        //
        final ParameterBlock pbjAffine = new ParameterBlock();
        Object interpolation = 
            ImageUtilities.NN_INTERPOLATION_HINT.get(JAI.KEY_INTERPOLATION);
        if (Math.abs(xTrans - (int) xTrans) < 1E-3
                && Math.abs(yTrans - (int) yTrans) < 1E-3
                && Math.abs(scaleX - 1) < 1E-6
                && Math.abs(scaleY - 1) < 1E-6)
        {

            // return the original image
            if (Math.abs(xTrans) < 1E-3 && Math.abs(yTrans) < 1E-3)
            {
                return image;
            }

            // translation
            pbjAffine.addSource(image)
                .add(new Float(xTrans))
                .add(new Float(yTrans))
                .add(interpolation);

            // avoid doing the color expansion now since it might not be needed
            final RenderingHints hints = (RenderingHints) 
                ImageUtilities.DONT_REPLACE_INDEX_COLOR_MODEL.clone();
            
            hints.put(JAI.KEY_IMAGE_LAYOUT, layout);
            return JAI.create("Translate", pbjAffine, hints);
        }
        
        // translation and scaling
        final AffineTransform tx = new AffineTransform(scaleX, 0, 0, scaleY,
                xTrans, yTrans);
        pbjAffine.addSource(image).add(tx).add(interpolation);
        // avoid doing the color expansion now since it might not be needed
        final RenderingHints hints = (RenderingHints) 
            ImageUtilities.DONT_REPLACE_INDEX_COLOR_MODEL.clone();

        // adding the capability to do a border extension which is great when
        // doing
        hints.add(ImageUtilities.EXTEND_BORDER_BY_COPYING);
        hints.put(JAI.KEY_IMAGE_LAYOUT, layout);
        return JAI.create("Affine", pbjAffine, hints);
    }
}