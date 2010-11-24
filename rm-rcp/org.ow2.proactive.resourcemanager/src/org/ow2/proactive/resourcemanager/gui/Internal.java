/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds 
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.resourcemanager.gui;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.ISharedImages;
import org.ow2.proactive.resourcemanager.Activator;
import org.ow2.proactive.resourcemanager.common.NodeState;


/**
 * Contains constants definitions and utility methods.
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>vbodnart 
 */
public final class Internal {

    /**
     * Each icon image is represented by a unique ID that can be used to find the corresponding 
     * image from the registry.
     */
    public static final String IMG_ADDNODE = "add_node.gif";
    public static final String IMG_BUSY = "busy.gif";
    public static final String IMG_COLLAPSEALL = "collapseall.gif";
    public static final String IMG_CONNECT = "connect.gif";
    public static final String IMG_CREATESOURCE = "create_source.gif";
    public static final String IMG_DISCONNECT = "disconnect.gif";
    public static final String IMG_DOWN = "down.gif";
    public static final String IMG_EXPANDALL = "expandall.gif";
    public static final String IMG_FREE = "free.gif";
    public static final String IMG_HOST = "host.gif";
    public static final String IMG_DEPLOYING = "pending.gif";
    public static final String IMG_LOST = "lost.gif";
    public static final String IMG_REMOVENODE = "remove_node.gif";
    public static final String IMG_REMOVESOURCE = "remove_source.gif";
    public static final String IMG_RMSHUTDOWN = "rm_shutdown.gif";
    public static final String IMG_SOURCE = "source.gif";
    public static final String IMG_TORELEASE = "to_release.gif";
    public static final String IMG_CONFIGURING = "configuring.gif";

    /**
     * Given a node state returns the corresponding image taken from the registry of this plugin.
     * <p>
     * In case of unknown state returns {@link ISharedImages.IMG_OBJS_ERROR_TSK}.
     * @param nodeState the state of a node
     * @return the corresponding image
     */
    public static Image getImageByNodeState(final NodeState nodeState) {
        switch (nodeState) {
            case CONFIGURING:
                return Activator.getDefault().getImageRegistry().get(Internal.IMG_CONFIGURING);
            case DOWN:
                return Activator.getDefault().getImageRegistry().get(Internal.IMG_DOWN);
            case FREE:
                return Activator.getDefault().getImageRegistry().get(Internal.IMG_FREE);
            case BUSY:
                return Activator.getDefault().getImageRegistry().get(Internal.IMG_BUSY);
            case TO_BE_REMOVED:
                return Activator.getDefault().getImageRegistry().get(Internal.IMG_TORELEASE);
            case LOST:
                return Activator.getDefault().getImageRegistry().get(Internal.IMG_LOST);
            case DEPLOYING:
                return Activator.getDefault().getImageRegistry().get(Internal.IMG_DEPLOYING);
            default:
                return Activator.getDefault().getImageRegistry().get(ISharedImages.IMG_OBJS_ERROR_TSK);
        }
    }

    /**
     * Convert a SWT Image to an AWT BufferedImage
     * @param data SWT data of the image
     * @return the corresponding image
     */
    public static BufferedImage convertToAWT(ImageData data) {
        ColorModel colorModel = null;
        PaletteData palette = data.palette;
        if (palette.isDirect) {
            colorModel = new DirectColorModel(data.depth, palette.redMask, palette.greenMask,
                palette.blueMask);
            BufferedImage bufferedImage = new BufferedImage(colorModel, colorModel
                    .createCompatibleWritableRaster(data.width, data.height), false, null);
            WritableRaster raster = bufferedImage.getRaster();
            int[] pixelArray = new int[3];
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    int pixel = data.getPixel(x, y);
                    RGB rgb = palette.getRGB(pixel);
                    pixelArray[0] = rgb.red;
                    pixelArray[1] = rgb.green;
                    pixelArray[2] = rgb.blue;
                    raster.setPixels(x, y, 1, 1, pixelArray);
                }
            }
            return bufferedImage;
        } else {
            RGB[] rgbs = palette.getRGBs();
            byte[] red = new byte[rgbs.length];
            byte[] green = new byte[rgbs.length];
            byte[] blue = new byte[rgbs.length];
            for (int i = 0; i < rgbs.length; i++) {
                RGB rgb = rgbs[i];
                red[i] = (byte) rgb.red;
                green[i] = (byte) rgb.green;
                blue[i] = (byte) rgb.blue;
            }
            if (data.transparentPixel != -1) {
                colorModel = new IndexColorModel(data.depth, rgbs.length, red, green, blue,
                    data.transparentPixel);
            } else {
                colorModel = new IndexColorModel(data.depth, rgbs.length, red, green, blue);
            }
            BufferedImage bufferedImage = new BufferedImage(colorModel, colorModel
                    .createCompatibleWritableRaster(data.width, data.height), false, null);
            WritableRaster raster = bufferedImage.getRaster();
            int[] pixelArray = new int[1];
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    int pixel = data.getPixel(x, y);
                    pixelArray[0] = pixel;
                    raster.setPixel(x, y, pixelArray);
                }
            }
            return bufferedImage;
        }
    }

}