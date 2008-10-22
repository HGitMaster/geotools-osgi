package org.geotools.coverage.io;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.geotools.data.Parameter;
import org.geotools.factory.Hints;
import org.opengis.util.ProgressListener;

/**
 * CoverageAccessFactory for working with formats based on a single URL.
 * 
 * @author Simone Giannecchini, GeoSolutions
 * @author Jody Garnett
 */
public interface FileBasedCoverageAccessFactory extends CoverageAccessFactory {
    /**
     * The list of filename extensions handled by this factory.
     * 
     * @return List of file extensions which can be read by this dataStore.
     */
    public List<String> getFileExtensions();

    /**
     * True if the {@link URL} can be handled by this factory.
     * 
     * @param f
     *                URL a {@link URL} to a real file (may not be local)
     * @param failIfNotExists
     *                tells the factory whether or not the request should fail
     *                for a non-existent {@link CoverageAccess}.
     * 
     * @return True when this dataStore can resolve and read the data specified
     *         by the {@link URL}.
     */
    public boolean canConnect(URL source, boolean failIfNotExists);

    /**
     * A DataStore attached to the provided {@link URL}, may be created if
     * needed.
     * 
     * @param url
     *                A {@link URL} to the data location for the single
     *                featureType of this DataStore
     * @param canCreate
     *                tells the factory whether or not the request should fail
     *                for a non-existent {@link CoverageAccess}.
     * @return Returns an AbstractFileDataStore created from the data source
     *         provided.
     * 
     * @throws IOException
     * 
     * @see AbstractFileDataStore
     */
    public CoverageAccess connect(URL source,
            Map<String, Parameter<?>> additionalParameters, Hints hints,
            final ProgressListener listener, boolean canCreate)
            throws IOException;

}
