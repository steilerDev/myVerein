/**
 * Copyright (C) 2015 Frank Steiler <frank@steilerdev.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.steilerdev.myVerein.server.model;

import com.mongodb.MongoException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * This bean is providing the needed operations performed on the GridFS of the MongoDB.
 */
public class GridFSRepository
{
    @Autowired
    private GridFsTemplate gridFS;

    private static Logger logger = LoggerFactory.getLogger(GridFSRepository.class);

    /**
     * The name of the club logo file stored within GridFS
     */
    private final String clubLogoFileName = "clubLogo";
    /**
     * A String containing the informal name of the format.
     */
    private final String clubLogoFileFormat = "png";
    private final String clubLogoFileFormatMIME = "image/png";

    /**
     * The name of the filename column within the database
     */
    private final String filenameColumn = "filename";

    public void deleteCurrentClubLogo()
    {
        gridFS.delete(new Query().addCriteria(Criteria.where(filenameColumn).is(clubLogoFileName)));
    }

    /**
     * This function gathers the current club logo. If several files or none matches the defined club logo filename, all of them get deleted.
     * @return The current club logo, or null if there is none or more than one.
     */
    public GridFSDBFile findClubLogo()
    {
        List<GridFSDBFile> clubLogoFiles;
        try
        {
            clubLogoFiles = gridFS.find(new Query().addCriteria(Criteria.where("filename").is(clubLogoFileName)));
        } catch (MongoTimeoutException e)
        {
            logger.warn("Timeout while trying to find club logo");
            return null;
        }

        if(clubLogoFiles == null || clubLogoFiles.isEmpty())
        {
            logger.warn("Unable to find any club logo");
            return null;
        } else if(clubLogoFiles.size() > 1)
        {
            logger.warn("Multiple files matching club logo name, deleting all");
            deleteCurrentClubLogo();
            return null;
        } else
        {
            return clubLogoFiles.get(0);
        }
    }

    public GridFSFile storeClubLogo(MultipartFile clubLogoFile) throws MongoException
    {
        if(!clubLogoFile.getContentType().startsWith("image"))
        {
            logger.warn("Trying to store a club logo, which is not an image");
            throw new MongoException("The file needs to be an image");
        } else if (!(clubLogoFile.getContentType().equals("image/jpeg") || clubLogoFile.getContentType().equals("image/png")))
        {
            logger.warn("Trying to store an incompatible image " + clubLogoFile.getContentType());
            throw new MongoException("The used image is not compatible, please use only PNG or JPG files");
        } else
        {
            File clubLogoTempFile = null;
            try
            {
                clubLogoTempFile = File.createTempFile("tempClubLogo", "png");
                clubLogoTempFile.deleteOnExit();
                if(clubLogoFile.getContentType().equals("image/png"))
                {
                    logger.debug("No need to convert club logo");
                    clubLogoFile.transferTo(clubLogoTempFile);
                } else
                {
                    logger.info("Converting club logo file to png");
                    //Reading, converting and writing club logo
                    ImageIO.write(ImageIO.read(clubLogoFile.getInputStream()), clubLogoFileFormat, clubLogoTempFile);
                }

                //Deleting current file
                deleteCurrentClubLogo();

                try(FileInputStream clubLogoStream = new FileInputStream(clubLogoTempFile))
                {
                    logger.debug("Saving club logo");
                    //Saving file
                    return gridFS.store(clubLogoStream, clubLogoFileName, clubLogoFileFormatMIME);
                }
            } catch (IOException e)
            {
                e.printStackTrace();
                throw new MongoException("Unable to store file");
            } finally
            {
                if(clubLogoTempFile != null)
                {
                    clubLogoTempFile.delete();
                }
            }
        }
    }

    /**
     * This function should delete the complete collection.
     * Todo: Check!
     */
    public void deleteAll()
    {
        gridFS.delete(new BasicQuery("{}"));
    }
}
