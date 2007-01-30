/***
 * Julia: France Telecom's implementation of the Fractal API
 * Copyright (C) 2001-2002 France Telecom R&D
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Contact: Eric.Bruneton@rd.francetelecom.com
 *
 * Author: Eric Bruneton
 */

package nonregressiontest.component.conform.components;

public interface I {

  void m (boolean v);
  void m (byte v);
  void m (char v);
  void m (short v);
  void m (int v);
  void m (long v);
  void m (float v);
  void m (double v);
  void m (String v);
  void m (String[] v);

  boolean  n (boolean v, String[] w);
  byte     n (byte v, String w);
  char     n (char v, double w);
  short    n (short v, float w);
  int      n (int v, long w);
  long     n (long v, int w);
  float    n (float v, short w);
  double   n (double v, char w);
  String   n (String v, byte w);
  String[] n (String[] v, boolean w);
}
