/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
 *   
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.image.io.hdf;

// JS2E dependencies
import java.util.List;
import java.lang.reflect.Array;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;

// HDF dependencies
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.Dataset; 
import ncsa.hdf.object.Group;
import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.FileFormat;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Errors;
import static org.geotools.resources.i18n.ErrorKeys.*;

// Sicade dependencies
import org.geotools.image.io.FileBasedReader;
import org.geotools.image.io.FileBasedReaderSpi;


/**
 * Implémentation par défaut des décodeurs d'images au format HDF. Dans la plupart des
 * cas, il ne sera pas nécessaire de créer des classes dérivées. Des classes dérivées
 * de {@link AbstractReaderSpi} suffisent.
 * 
 * @version $Id: DefaultReader.java 30680 2008-06-13 10:22:22Z acuster $
 * @author Antoine Hnawia
 * @author Martin Desruisseaux (IRD)
 */
final class DefaultReader extends FileBasedReader {
    /**
     * Le format des données à lire.
     */
    private FileFormat format;

    /**
     * L'ensemble des données du phénomène étudié.
     * <p>
     * <b>Note:</b> Ne pas appeler {@link Dataset#init} explicitement. C'est sensé être fait
     * automatiquement par la bibliothèque HDF. L'expérience montre qu'un appel explicite à
     * {@code init()} fonctionne sous Windows, mais cause un crash de la JVM sous Linux Gentoo.
     *
     * @see #prepareDataset
     */
    private Dataset dataset;

    /**
     * Les indicateurs de qualité pour chaque donnée, ou {@code null} s'il n'y en a pas.
     *
     * @see #prepareDataset
     */
    private Dataset qualityDataset;

    /**
     * Indique le nombre de bits sur lequel est codée l'information sans la qualité.
     * La valeur par défaut est 8.
     *
     * @see #setBitCount
     */
    private int dataBitCount = 8;

    /**
     * Indique le nombre de bits sur lequel est codé la qualité. La valeur par défaut est 0,
     * c'est à dire une absence d'informations concernant la qualité des données.
     *
     * @see #setBitCount
     */
    private int qualityBitCount = 0;

    /** 
     * Construit un nouveau décodeur HDF. Les classes dérivées devraient appeler {@link #setBitCount
     * setBitCount} après la construction, ou au moins avant le premier appel de {@link #read read}.
     *
     * @param spi Une description du service fournit par ce décodeur.
     */
    public DefaultReader(final AbstractReaderSpi spi) {
        super(spi);
    }

    /**
     * Spécifie le nombre de bits sur lesquels sont codés les données. Si cette méthode n'est
     * jamais appelée, alors la valeur par défaut est de 8 bits pour les données et 0 bits pour
     * la qualité.
     *
     * @param  data      Nombre de bits sur lequel est codée l'information sans la qualité.
     * @param  quality   Nombre de bits sur lequel est codé la qualité.
     * @throws IllegalArgumentException si un des arguments spécifié est négatif.
     */
    protected final void setBitCount(final int data, final int quality) throws IllegalArgumentException {
        if (data < 0 || data >= Integer.SIZE) {
            throw new IllegalArgumentException(Errors.format(ILLEGAL_ARGUMENT_$2, "data", data));
        }
        if (quality < 0 || data+quality >= Integer.SIZE) {
            throw new IllegalArgumentException(Errors.format(ILLEGAL_ARGUMENT_$2, "quality", quality));
        }
        dataBitCount    = data;
        qualityBitCount = quality;
    }

    /**
     * Spécifie la source des données à utiliser en entrée. Cette source doit être un objet de
     * type {@link File}.
     */
    @Override
    public void setInput(final Object input, final boolean seekForwardOnly, final boolean ignoreMetadata) {
        super.setInput(input, seekForwardOnly, ignoreMetadata);
        close(qualityDataset); qualityDataset = null;
        close(dataset);        dataset        = null;
    }

    /**
     * Retourne la largeur de l'image.
     */
    public int getWidth(final int imageIndex) throws IOException {
        prepareDataset(imageIndex, false);
        return dataset.getWidth();
    }

    /**
     * Retourne la hauteur de l'image.
     */
    public int getHeight(final int imageIndex) throws IOException {
        prepareDataset(imageIndex, false);
        return dataset.getHeight();
    }

    /**
     * Construit une image à partir des paramètre de lecture spécifiés.
     *
     * @throws  IOException Si la lecture de l'image a échouée.
     */
    public BufferedImage read(final int imageIndex, final ImageReadParam param) throws IOException {
        clearAbortRequest();
        checkReadParamBandSettings(param, 1, 1);
        prepareDataset(imageIndex, true);
        final int            width  = dataset.getWidth();
        final int            height = dataset.getHeight();
        final BufferedImage  image  = getDestination(param, getImageTypes(imageIndex), width, height);
        final WritableRaster raster = image.getRaster();
        final Rectangle   srcRegion = new Rectangle();
        final Rectangle  destRegion = new Rectangle();
        final int strideX, strideY;
        if (param != null) {
            strideX = param.getSourceXSubsampling();
            strideY = param.getSourceYSubsampling();
        } else {
            strideX = 1;
            strideY = 1;
        }
        computeRegions(param, width, height, image, srcRegion, destRegion);
        processImageStarted(imageIndex);
        /*
         * Ici on fixe les parmatères tels qu'au prochain appel de dataset.getData(),
         * l'API HDF va nous permettre de récupérer les sizes[0] x sizes[1] valeurs entières 
         * de la sous région dont le point haut gauche a pour coordonnées start[0], start[1] 
         * et dont les largeur et hauteur sont respectivement size[0], size[1]
         * 
         * ATTENTION - ATTENTION : Il faut prendre la convention matricielle ligne colonne 
         *                         c'est à dire start[0] = y et start[1] = x !!!!!
         */
        final long[] start  = dataset.getStartDims();
        final long[] stride = dataset.getStride();
        final long[] sizes  = dataset.getSelectedDims();
        start [0] = srcRegion.y;
        start [1] = srcRegion.x;
        sizes [0] = srcRegion.height;
        sizes [1] = srcRegion.width;
        stride[0] = strideY;
        stride[1] = strideX;
        final Object data, quality;
        try {
            data = dataset.read();
            quality = (qualityDataset != null) ? qualityDataset.read() : null;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw hdfFailure(e);
        }
        final int dataMask          = getMask(dataset);
        final int qualityMask       = getMask(qualityDataset);
        final int targetDataMask    = ~((1 << dataBitCount) - 1);
        final int targetQualityMask = ~((1 << qualityBitCount) - 1);
        /*
         * Maintenant que l'ensemble des données ont été obtenues, prépare la copie vers le raster.
         * Si les entiers sont non-signés, on utilisera un masque pour supprimer le signe. Note: on
         * n'utilise pas Dataset.convertFromUnsignedC(Object) afin d'éviter la création d'un tableau
         * temporaire qui peut être volumineux.
         */
        int index = 0;
        final float toPercent = 100f / Array.getLength(data);
        final int xmax = destRegion.x + destRegion.width;
        final int ymax = destRegion.y + destRegion.height;
        for (int y=destRegion.y; y<ymax; y++) {
            for (int x=destRegion.x; x<xmax; x++) {
                int value = Array.getInt(data, index) & dataMask;
                if (quality != null) {
                    if ((value & targetDataMask) != 0) {
                        throw new IIOException("La valeur " + value + " à la position (" + x + ',' + y +
                                               ") n'est pas un entier non-signé sur " + dataBitCount + " bits.");
                    }
                    final int q = Array.getInt(quality, index) & qualityMask;
                    if ((q & targetQualityMask) != 0) {
                        throw new IIOException("L'indicateur de qualité " + q + " à la position (" + x + ',' + y +
                                               ") n'est pas un entier non-signé sur " + qualityBitCount + " bits.");
                    }
                    value |= (q << dataBitCount);
                }
                raster.setSample(x, y, 0, value);
                index++;
            }
            processImageProgress(index * toPercent);
            if (abortRequested()) {
                processReadAborted();
                return image;
            }
        }
        processImageComplete();
        assert index == Array.getLength(data) : index;
        return image;
    }

    /**
     * Vérifie que les données ont bien été chargée dans {@link #dataset} pour l'image spécifiée.
     * Si les données ont déjà été chargée lors d'un appel précédent, alors cette méthode ne fait
     * rien.
     * <p>
     * Certaines données enregistrent dans un fichier séparé des indicateurs de qualité pour chaque
     * pixel. Si ces informations existent, alors le paramètre {@code includeQuality} indique s'il
     * faut procéder à leur chargement ou pas.
     * 
     * @param   imageIndex L'index de l'image à traiter.
     * @param   {@code true} pour procéder aussi au chargement des indicateurs de qualité.
     * @throws  IndexOutOfBoundsException Si {@code indexImage} est différent de 0,
     *          car on considère qu'il n'y a qu'une image par fichier HDF.
     * @throws  IllegalStateException Si le champ {@link #input} n'a pas été initialisé via
     *          {@link #setInput setInput(...)}.
     * @throws  IIOException Si le fichier HDF ne semble pas correct.
     * @throws  IOException Si la lecture a échouée pour une autre raison.
     */
    private void prepareDataset(final int imageIndex, final boolean includeQuality) throws IOException {
        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException(Errors.format(ILLEGAL_ARGUMENT_$2, "imageIndex", imageIndex));
        }
        if (dataset == null) {
            final File inputFile = getInputFile();
            dataset = open(inputFile.getPath());
            if (dataset == null) {
                throw new IOException("Aucune donnée n'a été trouvée dans le fichier HDF.");
            }
            if (originatingProvider instanceof AbstractReaderSpi) {
                checkName(inputFile, dataset, ((AbstractReaderSpi) originatingProvider).dataName);
            }
        }
        /*
         * Procède au chargement des indicateurs de qualités, s'ils ont été demandés.
         * Le nom de l'objet 'Dataset' obtenu sera comparé avec celui qui était attendu,
         * et les indicateurs de qualités rejetés s'ils ne correspondent pas.
         */
        if (includeQuality && qualityDataset==null && originatingProvider instanceof AbstractReaderSpi) {
            final File inputFile = getInputFile();
            final File qualityFile;
            if (isTemporaryFile()) {
                qualityFile = null; // TODO: prendre en compte les indicateurs de qualité.
            } else {
                qualityFile = ((AbstractReaderSpi) originatingProvider).getQualityFile(inputFile);
            }
            if (qualityFile != null) {
                if (!qualityFile.isFile()) {
                    processWarningOccurred("Le fichier d'indicateurs de qualité \"" +
                                           qualityFile.getName() + "\" n'a pas été trouvé.");
                } else {
                    qualityDataset = open(qualityFile.getPath());
                    if (originatingProvider instanceof AbstractReaderSpi) {
                        checkName(qualityFile, qualityDataset, ((AbstractReaderSpi) originatingProvider).qualityName);
                    }
                }
            }
        }
    }

    /**
     * Renvoie un objet {@link Dataset} à partir d'un objet {@link HObject}.
     *
     * @param   hobject L'objet {@link HObject} à parcourir.
     * @return  L'objet {@link Dataset} contenu dans l'objet {@link HObject}, ou {@code null}
     *          si aucun n'a été trouvé.
     * @throws  IIOException si une erreur est survenu lors de l'examen de {@code object}.
     */
    private static Dataset getDataset(final HObject object) throws IIOException {
        if (object instanceof Dataset) {
            return (Dataset) object;
        }
        if (object instanceof Group) try {
            @SuppressWarnings("unchecked")
            final List<HObject> members = ((Group) object).getMemberList();
            for (final HObject member : members) {
                final Dataset candidate = getDataset(member);
                if (candidate != null) {
                    return candidate;
                }
            }
        } catch (ClassCastException exception) {
            throw hdfFailure(exception);
        }
        return null;
    }

    /**
     * Retourne le masque à appliquer sur les données lues pour éviter que le Java ne transforme
     * certains entiers non-signés en valeurs négatives.
     */
    private static int getMask(final Dataset dataset) throws IIOException {
        if (dataset != null) {
            final Datatype type = dataset.getDatatype();
            final int      size = type.getDatatypeSize() * Byte.SIZE;
            if (size<1 || size>Integer.SIZE) {
                throw new IIOException("Les entiers sur " + size + " bits ne sont pas supportées.");
            }
            if (type.isUnsigned()) {
                if (size == Integer.SIZE) {
                    throw new IIOException("Les entiers non-signés sur " + size + " bits ne sont pas supportées.");
                }
                return (1 << size) - 1;
            }
        }
        return ~0;
    }

    /**
     * Vérifie que le nom du {@code dataset} est bien celui que l'on attend ({@code expected}).
     * 
     * @param   file         Le nom du fichier HDF traité.
     * @param   dataset      Les données obtenues.
     * @param   datasetName  Le nom du dataset attendu. Actuellement, pour la température ce nom est 
     *                       {@code sst} et pour la chlorophylle le nom est {@code l3m_data}.
     * @throws  IIOException Si le nom du dataset n'est pas celui que l'on attendait.
     */
    private static void checkName(final File file, final Dataset dataset, String expected) throws IIOException {
        if (dataset != null && expected != null) {
            final String name = dataset.getName().trim();
            if (name != null) {
                expected = expected.trim();
                if (!name.equalsIgnoreCase(expected)) {
                    throw new IIOException("Le fichier \"" + file.getName() +
                            "\" ne semble par contenir les données attendues. " +
                            "Ses données portent le nom \"" + name +
                            "\" alors que l'on attendait \"" + expected + "\".");
                }
            }
        }
    }

    /**
     * Lance une exception un peu plus explicite lorsqu'une erreur est survenue
     * lors de la lecture d'un fichier HDF.
     */
    private static IIOException hdfFailure(final Exception e) {
        return new IIOException("Echec lors de la lecture du fichier HDF", e);
    }

    /**
     * Ouvre le fichier spécifié et retourne les données qu'il contient, ou {@code null} si aucune
     * donnée n'a été trouvée.
     *
     * @throws IOException si l'ouverture et la lecture du fichier a échoué.
     */
    private Dataset open(final String filename) throws IOException {
        if (format == null) {
            format = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF4);
            if (format == null) {
                throw new IIOException("Format non-disponible.");
            }
        }
        final FileFormat file;
        final HObject object;
        try {
            file = format.open(filename, FileFormat.READ);
            object = file.get("/");
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw hdfFailure(e);
        }
        final Dataset data = getDataset(object);
        assert data.getFileFormat() == file;
        return data;
    }

    /**
     * Ferme le fichier associé au dataset spécifié.
     */
    private static void close(final Dataset dataset) {
        if (dataset != null) {
            final FileFormat format = dataset.getFileFormat();
            if (format != null) try {
                format.close();
            } catch (Exception e) {
                /*
                 * Ignore cette erreur (excepté pour l'écriture dans le journal), étant
                 * donné que l'on ferme ce fichier dans l'intention d'en ouvrir un autre.
                 */
                Utilities.unexpectedException("net.sicade.image.io.hdf", "DefaultReader", "setInput", e);
            }
        }
    }

    /**
     * Libère toutes les ressources utilisées par cet objet.
     */
    @Override
    public void dispose() {
        super.dispose();
        format = null;
    }
}
