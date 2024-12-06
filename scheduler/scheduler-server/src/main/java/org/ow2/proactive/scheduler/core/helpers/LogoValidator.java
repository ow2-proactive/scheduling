/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scheduler.core.helpers;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.ImageInfo;
import org.apache.commons.imaging.Imaging;


public class LogoValidator {

    private static final int MAX_WIDTH = 1500;

    private static final int MAX_HEIGHT = 1500;

    /**
     *   Checks that the input image is of the correct format (PNG or JPEG), size and MIME type.
     *
     *   @param byteArrayImage the PNG or JPEG image as a byte array
     *   @return a byte array of the image as a PNG
     */
    public static byte[] checkImage(byte[] byteArrayImage) throws IOException {

        ImageInfo imageInfo = Imaging.getImageInfo(byteArrayImage);
        BufferedImage image = Imaging.getBufferedImage(byteArrayImage);

        byte[] result = byteArrayImage;

        // If the image is a JPEG, convert it to PNG
        if (imageInfo.getFormat() == ImageFormats.JPEG) {

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Imaging.writeImage(image, byteArrayOutputStream, ImageFormats.PNG);
            result = byteArrayOutputStream.toByteArray();

            // If its neither JPEG or PNG, throw exception
        } else if (imageInfo.getFormat() != ImageFormats.PNG) {
            throw new IllegalArgumentException("Image has bad format. Must be PNG or JPEG");
        }

        // Check that the image is in the authorized size
        if (image.getWidth() > MAX_WIDTH || image.getHeight() > MAX_HEIGHT) {
            throw new IllegalArgumentException("Image is too big. Max size is " + MAX_WIDTH + "x" + MAX_HEIGHT);
        }

        // Check MIME type is of image
        if (imageInfo.getMimeType() == null || !imageInfo.getMimeType().startsWith("image/")) {
            throw new IllegalArgumentException("Image has a bad MIME type: " + imageInfo.getMimeType());
        }

        return result;
    }
}
