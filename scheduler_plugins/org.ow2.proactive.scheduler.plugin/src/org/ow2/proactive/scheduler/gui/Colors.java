/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.ow2.proactive.scheduler.gui;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;


/**
 * This class only contains color constant.
 *
 * <h1>Color Codes</h1>
 * <table border = "1">
 * <tr>
 * <th>rgb
 * <th>Hexadecimal
 * <th>Name
 * <th>Appearance
 * <tr>
 * <td>255 250 250
 * <td>fffafa
 * <td>snow
 * <td bgcolor=fffafa>&nbsp;
 * <tr>
 * <td>248 248 255
 * <td>f8f8ff
 * <td>ghost white
 * <td bgcolor=f8f8ff>&nbsp;
 * <tr>
 * <td>245 245 245
 * <td>f5f5f5
 * <td>white smoke
 * <td bgcolor=f5f5f5>&nbsp;
 * <tr>
 * <td>220 220 220
 * <td>dcdcdc
 * <td>gainsboro
 * <td bgcolor=dcdcdc>&nbsp;
 * <tr>
 * <td>255 250 240
 * <td>fffaf0
 * <td>floral white
 * <td bgcolor=fffaf0>&nbsp;
 * <tr>
 * <td>253 245 230
 * <td>fdf5e6
 * <td>old lace
 * <td bgcolor=fdf5e6>&nbsp;
 * <tr>
 * <td>250 240 230
 * <td>faf0e6
 * <td>linen
 * <td bgcolor=faf0e6>&nbsp;
 * <tr>
 * <td>250 235 215
 * <td>faebd7
 * <td>antique white
 * <td bgcolor=faebd7>&nbsp;
 * <tr>
 * <td>255 239 213
 * <td>ffefd5
 * <td>papaya whip
 * <td bgcolor=ffefd5>&nbsp;
 * <tr>
 * <td>255 235 205
 * <td>ffebcd
 * <td>blanched almond
 * <td bgcolor=ffebcd>&nbsp;
 * <tr>
 * <td>255 228 196
 * <td>ffe4c4
 * <td>bisque
 * <td bgcolor=ffe4c4>&nbsp;
 * <tr>
 * <td>255 218 185
 * <td>ffdab9
 * <td>peach puff
 * <td bgcolor=ffdab9>&nbsp;
 * <tr>
 * <td>255 222 173
 * <td>ffdead
 * <td>navajo white
 * <td bgcolor=ffdead>&nbsp;
 * <tr>
 * <td>255 228 181
 * <td>ffe4b5
 * <td>moccasin
 * <td bgcolor=ffe4b5>&nbsp;
 * <tr>
 * <td>255 248 220
 * <td>fff8dc
 * <td>cornsilk
 * <td bgcolor=fff8dc>&nbsp;
 * <tr>
 * <td>255 255 240
 * <td>fffff0
 * <td>ivory
 * <td bgcolor=fffff0>&nbsp;
 * <tr>
 * <td>255 250 205
 * <td>fffacd
 * <td>lemon chiffon
 * <td bgcolor=fffacd>&nbsp;
 * <tr>
 * <td>255 245 238
 * <td>fff5ee
 * <td>seashell
 * <td bgcolor=fff5ee>&nbsp;
 * <tr>
 * <td>240 255 240
 * <td>f0fff0
 * <td>honeydew
 * <td bgcolor=f0fff0>&nbsp;
 * <tr>
 * <td>245 255 250
 * <td>f5fffa
 * <td>mint cream
 * <td bgcolor=f5fffa>&nbsp;
 * <tr>
 * <td>240 255 255
 * <td>f0ffff
 * <td>azure
 * <td bgcolor=f0ffff>&nbsp;
 * <tr>
 * <td>240 248 255
 * <td>f0f8ff
 * <td>alice blue
 * <td bgcolor=f0f8ff>&nbsp;
 * <tr>
 * <td>230 230 250
 * <td>e6e6fa
 * <td>lavender
 * <td bgcolor=e6e6fa>&nbsp;
 * <tr>
 * <td>255 240 245
 * <td>fff0f5
 * <td>lavender blush
 * <td bgcolor=fff0f5>&nbsp;
 * <tr>
 * <td>255 228 225
 * <td>ffe4e1
 * <td>misty rose
 * <td bgcolor=ffe4e1>&nbsp;
 * <tr>
 * <td>255 255 255
 * <td>ffffff
 * <td>white
 * <td bgcolor=ffffff>&nbsp;
 * <tr>
 * <td> 0 0 0
 * <td>000000
 * <td>black
 * <td bgcolor=000000>&nbsp;
 * <tr>
 * <td> 47 79 79
 * <td>2f4f4f
 * <td>dark slate grey
 * <td bgcolor=2f4f4f>&nbsp;
 * <tr>
 * <td>105 105 105
 * <td>696969
 * <td>dim grey
 * <td bgcolor=696969>&nbsp;
 * <tr>
 * <td>112 128 144
 * <td>708090
 * <td>slate grey
 * <td bgcolor=708090>&nbsp;
 * <tr>
 * <td>119 136 153
 * <td>778899
 * <td>light slate grey
 * <td bgcolor=778899>&nbsp;
 * <tr>
 * <td>211 211 211
 * <td>d3d3d3
 * <td>light grey
 * <td bgcolor=d3d3d3>&nbsp;
 * <tr>
 * <td> 25 25 112
 * <td>191970
 * <td>midnight blue
 * <td bgcolor=191970>&nbsp;
 * <tr>
 * <td> 0 0 128
 * <td>000080
 * <td>navy
 * <td bgcolor=000080>&nbsp;
 * <tr>
 * <td> 0 0 128
 * <td>000080
 * <td>navy blue
 * <td bgcolor=000080>&nbsp;
 * <tr>
 * <td>100 149 237
 * <td>6495ed
 * <td>cornflower blue
 * <td bgcolor=6495ed>&nbsp;
 * <tr>
 * <td> 72 61 139
 * <td>483d8b
 * <td>dark slate blue
 * <td bgcolor=483d8b>&nbsp;
 * <tr>
 * <td>106 90 205
 * <td>6a5acd
 * <td>slate blue
 * <td bgcolor=6a5acd>&nbsp;
 * <tr>
 * <td>123 104 238
 * <td>7b68ee
 * <td>medium slate blue
 * <td bgcolor=7b68ee>&nbsp;
 * <tr>
 * <td>132 112 255
 * <td>8470ff
 * <td>light slate blue
 * <td bgcolor=8470ff>&nbsp;
 * <tr>
 * <td> 0 0 205
 * <td>0000cd
 * <td>medium blue
 * <td bgcolor=0000cd>&nbsp;
 * <tr>
 * <td> 65 105 225
 * <td>4169e1
 * <td>royal blue
 * <td bgcolor=4169e1>&nbsp;
 * <tr>
 * <td> 0 0 255
 * <td>0000ff
 * <td>blue
 * <td bgcolor=0000ff>&nbsp;
 * <tr>
 * <td> 30 144 255
 * <td>1e90ff
 * <td>dodger blue
 * <td bgcolor=1e90ff>&nbsp;
 * <tr>
 * <td> 0 191 255
 * <td>00bfff
 * <td>deep sky blue
 * <td bgcolor=00bfff>&nbsp;
 * <tr>
 * <td>135 206 235
 * <td>87ceeb
 * <td>sky blue
 * <td bgcolor=87ceeb>&nbsp;
 * <tr>
 * <td>135 206 250
 * <td>87cefa
 * <td>light sky blue
 * <td bgcolor=87cefa>&nbsp;
 * <tr>
 * <td>176 196 222
 * <td>b0c4de
 * <td>light steel blue
 * <td bgcolor=b0c4de>&nbsp;
 * <tr>
 * <td>173 216 230
 * <td>add8e6
 * <td>light blue
 * <td bgcolor=add8e6>&nbsp;
 * <tr>
 * <td>176 224 230
 * <td>b0e0e6
 * <td>powder blue
 * <td bgcolor=b0e0e6>&nbsp;
 * <tr>
 * <td> 0 206 209
 * <td>00ced1
 * <td>dark turquoise
 * <td bgcolor=00ced1>&nbsp;
 * <tr>
 * <td> 72 209 204
 * <td>48d1cc
 * <td>medium turquoise
 * <td bgcolor=48d1cc>&nbsp;
 * <tr>
 * <td> 64 224 208
 * <td>40e0d0
 * <td>turquoise
 * <td bgcolor=40e0d0>&nbsp;
 * <tr>
 * <td> 0 255 255
 * <td>00ffff
 * <td>cyan
 * <td bgcolor=00ffff>&nbsp;
 * <tr>
 * <td>224 255 255
 * <td>e0ffff
 * <td>light cyan
 * <td bgcolor=e0ffff>&nbsp;
 * <tr>
 * <td> 95 158 160
 * <td>5f9ea0
 * <td>cadet blue
 * <td bgcolor=5f9ea0>&nbsp;
 * <tr>
 * <td>102 205 170
 * <td>66cdaa
 * <td>medium aquamarine
 * <td bgcolor=66cdaa>&nbsp;
 * <tr>
 * <td>127 255 212
 * <td>7fffd4
 * <td>aquamarine
 * <td bgcolor=7fffd4>&nbsp;
 * <tr>
 * <td> 0 100 0
 * <td>006400
 * <td>dark green
 * <td bgcolor=006400>&nbsp;
 * <tr>
 * <td> 85 107 47
 * <td>556b2f
 * <td>dark olive green
 * <td bgcolor=556b2f>&nbsp;
 * <tr>
 * <td>143 188 143
 * <td>8fbc8f
 * <td>dark sea green
 * <td bgcolor=8fbc8f>&nbsp;
 * <tr>
 * <td> 46 139 87
 * <td>2e8b57
 * <td>sea green
 * <td bgcolor=2e8b57>&nbsp;
 * <tr>
 * <td> 60 179 113
 * <td>3cb371
 * <td>medium sea green
 * <td bgcolor=3cb371>&nbsp;
 * <tr>
 * <td> 32 178 170
 * <td>20b2aa
 * <td>light sea green
 * <td bgcolor=20b2aa>&nbsp;
 * <tr>
 * <td>152 251 152
 * <td>98fb98
 * <td>pale green
 * <td bgcolor=98fb98>&nbsp;
 * <tr>
 * <td> 0 255 127
 * <td>00ff7f
 * <td>spring green
 * <td bgcolor=00ff7f>&nbsp;
 * <tr>
 * <td>124 252 0
 * <td>7cfc00
 * <td>lawn green
 * <td bgcolor=7cfc00>&nbsp;
 * <tr>
 * <td> 0 255 0
 * <td>00ff00
 * <td>green
 * <td bgcolor=00ff00>&nbsp;
 * <tr>
 * <td>127 255 0
 * <td>7fff00
 * <td>chartreuse
 * <td bgcolor=7fff00>&nbsp;
 * <tr>
 * <td> 0 250 154
 * <td>00fa9a
 * <td>medium spring green
 * <td bgcolor=00fa9a>&nbsp;
 * <tr>
 * <td>173 255 47
 * <td>adff2f
 * <td>green yellow
 * <td bgcolor=adff2f>&nbsp;
 * <tr>
 * <td> 50 205 50
 * <td>32cd32
 * <td>lime green
 * <td bgcolor=32cd32>&nbsp;
 * <tr>
 * <td>154 205 50
 * <td>9acd32
 * <td>yellow green
 * <td bgcolor=9acd32>&nbsp;
 * <tr>
 * <td> 34 139 34
 * <td>228b22
 * <td>forest green
 * <td bgcolor=228b22>&nbsp;
 * <tr>
 * <td>107 142 35
 * <td>6b8e23
 * <td>olive drab
 * <td bgcolor=6b8e23>&nbsp;
 * <tr>
 * <td>189 183 107
 * <td>bdb76b
 * <td>dark khaki
 * <td bgcolor=bdb76b>&nbsp;
 * <tr>
 * <td>240 230 140
 * <td>f0e68c
 * <td>khaki
 * <td bgcolor=f0e68c>&nbsp;
 * <tr>
 * <td>238 232 170
 * <td>eee8aa
 * <td>pale goldenrod
 * <td bgcolor=eee8aa>&nbsp;
 * <tr>
 * <td>255 255 224
 * <td>ffffe0
 * <td>light yellow
 * <td bgcolor=ffffe0>&nbsp;
 * <tr>
 * <td>255 255 0
 * <td>ffff00
 * <td>yellow
 * <td bgcolor=ffff00>&nbsp;
 * <tr>
 * <td>255 215 0
 * <td>ffd700
 * <td> gold
 * <td bgcolor=ffd700>&nbsp;
 * <tr>
 * <td>238 221 130
 * <td>eedd82
 * <td>light goldenrod
 * <td bgcolor=eedd82>&nbsp;
 * <tr>
 * <td>218 165 32
 * <td>daa520
 * <td>goldenrod
 * <td bgcolor=daa520>&nbsp;
 * <tr>
 * <td>184 134 11
 * <td>b8860b
 * <td>dark goldenrod
 * <td bgcolor=b8860b>&nbsp;
 * <tr>
 * <td>188 143 143
 * <td>bc8f8f
 * <td>rosy brown
 * <td bgcolor=bc8f8f>&nbsp;
 * <tr>
 * <td>139 69 19
 * <td>8b4513
 * <td>saddle brown
 * <td bgcolor=8b4513>&nbsp;
 * <tr>
 * <td>160 82 45
 * <td>a0522d
 * <td>sienna
 * <td bgcolor=a0522d>&nbsp;
 * <tr>
 * <td>205 133 63
 * <td>cd853f
 * <td>peru
 * <td bgcolor=cd853f>&nbsp;
 * <tr>
 * <td>222 184 135
 * <td>deb887
 * <td>burlywood
 * <td bgcolor=deb887>&nbsp;
 * <tr>
 * <td>245 245 220
 * <td>f5f5dc
 * <td>beige
 * <td bgcolor=f5f5dc>&nbsp;
 * <tr>
 * <td>245 222 179
 * <td>f5deb3
 * <td>wheat
 * <td bgcolor=f5deb3>&nbsp;
 * <tr>
 * <td>244 164 96
 * <td>f4a460
 * <td>sandy brown
 * <td bgcolor=f4a460>&nbsp;
 * <tr>
 * <td>210 180 140
 * <td>d2b48c
 * <td>tan
 * <td bgcolor=d2b48c>&nbsp;
 * <tr>
 * <td>210 105 30
 * <td>d2691e
 * <td>chocolate
 * <td bgcolor=d2691e>&nbsp;
 * <tr>
 * <td>178 34 34
 * <td>b22222
 * <td>firebrick
 * <td bgcolor=b22222>&nbsp;
 * <tr>
 * <td>165 42 42
 * <td>a52a2a
 * <td>brown
 * <td bgcolor=a52a2a>&nbsp;
 * <tr>
 * <td>233 150 122
 * <td>e9967a
 * <td>dark salmon
 * <td bgcolor=e9967a>&nbsp;
 * <tr>
 * <td>250 128 114
 * <td>fa8072
 * <td>salmon
 * <td bgcolor=fa8072>&nbsp;
 * <tr>
 * <td>255 160 122
 * <td>ffa07a
 * <td>light salmon
 * <td bgcolor=ffa07a>&nbsp;
 * <tr>
 * <td>255 165 0
 * <td>ffa500
 * <td>orange
 * <td bgcolor=ffa500>&nbsp;
 * <tr>
 * <td>255 140 0
 * <td>ff8c00
 * <td>dark orange
 * <td bgcolor=ff8c00>&nbsp;
 * <tr>
 * <td>255 127 80
 * <td>ff7f50
 * <td>coral
 * <td bgcolor=ff7f50>&nbsp;
 * <tr>
 * <td>240 128 128
 * <td>f08080
 * <td>light coral
 * <td bgcolor=f08080>&nbsp;
 * <tr>
 * <td>255 99 71
 * <td>ff6347
 * <td>tomato
 * <td bgcolor=ff6347>&nbsp;
 * <tr>
 * <td>255 69 0
 * <td>ff4500
 * <td>orange red
 * <td bgcolor=ff4500>&nbsp;
 * <tr>
 * <td>255 0 0
 * <td>ff0000
 * <td>red
 * <td bgcolor=ff0000>&nbsp;
 * <tr>
 * <td>255 105 180
 * <td>ff69b4
 * <td>hot pink
 * <td bgcolor=ff69b4>&nbsp;
 * <tr>
 * <td>255 20 147
 * <td>ff1493
 * <td>deep pink
 * <td bgcolor=ff1493>&nbsp;
 * <tr>
 * <td>255 192 203
 * <td>ffc0cb
 * <td>pink
 * <td bgcolor=ffc0cb>&nbsp;
 * <tr>
 * <td>219 112 147
 * <td>db7093
 * <td>pale violet red
 * <td bgcolor=db7093>&nbsp;
 * <tr>
 * <td>176 48 96
 * <td>b03060
 * <td>maroon
 * <td bgcolor=b03060>&nbsp;
 * <tr>
 * <td>199 21 133
 * <td>c71585
 * <td>medium violet red
 * <td bgcolor=c71585>&nbsp;
 * <tr>
 * <td>208 32 144
 * <td>d02090
 * <td>violet red
 * <td bgcolor=d02090>&nbsp;
 * <tr>
 * <td>255 0 255
 * <td>ff00ff
 * <td>magenta
 * <td bgcolor=ff00ff>&nbsp;
 * <tr>
 * <td>238 130 238
 * <td>ee82ee
 * <td>violet
 * <td bgcolor=ee82ee>&nbsp;
 * <tr>
 * <td>221 160 221
 * <td>dda0dd
 * <td>plum
 * <td bgcolor=dda0dd>&nbsp;
 * <tr>
 * <td>218 112 214
 * <td>da70d6
 * <td>orchid
 * <td bgcolor=da70d6>&nbsp;
 * <tr>
 * <td>186 85 211
 * <td>ba55d3
 * <td>medium orchid
 * <td bgcolor=ba55d3>&nbsp;
 * <tr>
 * <td>153 50 204
 * <td>9932cc
 * <td>dark orchid
 * <td bgcolor=9932cc>&nbsp;
 * <tr>
 * <td>148 0 211
 * <td>9400d3
 * <td>dark violet
 * <td bgcolor=9400d3>&nbsp;
 * <tr>
 * <td>160 32 240
 * <td>a020f0
 * <td>purple
 * <td bgcolor=a020f0>&nbsp;
 * <tr>
 * <td>147 112 219
 * <td>9370db
 * <td>medium purple
 * <td bgcolor=9370db>&nbsp;
 * <tr>
 * <td>216 191 216
 * <td>d8bfd8
 * <td>thistle
 * <td bgcolor=d8bfd8>&nbsp;
 * <tr>
 * <td>255 250 250
 * <td>fffafa
 * <td>snow1
 * <td bgcolor=fffafa>&nbsp;
 * <tr>
 * <td>238 233 233
 * <td>eee9e9
 * <td>snow2
 * <td bgcolor=eee9e9>&nbsp;
 * <tr>
 * <td>205 201 201
 * <td>cdc9c9
 * <td>snow3
 * <td bgcolor=cdc9c9>&nbsp;
 * <tr>
 * <td>139 137 137
 * <td>8b8989
 * <td>snow4
 * <td bgcolor=8b8989>&nbsp;
 * <tr>
 * <td>255 245 238
 * <td>fff5ee
 * <td>seashell1
 * <td bgcolor=fff5ee>&nbsp;
 * <tr>
 * <td>238 229 222
 * <td>eee5de
 * <td>seashell2
 * <td bgcolor=eee5de>&nbsp;
 * <tr>
 * <td>205 197 191
 * <td>cdc5bf
 * <td>seashell3
 * <td bgcolor=cdc5bf>&nbsp;
 * <tr>
 * <td>139 134 130
 * <td>8b8682
 * <td>seashell4
 * <td bgcolor=8b8682>&nbsp;
 * <tr>
 * <td>238 213 183
 * <td>eed5b7
 * <td>bisque2
 * <td bgcolor=eed5b7>&nbsp;
 * <tr>
 * <td>205 183 158
 * <td>cdb79e
 * <td>bisque3
 * <td bgcolor=cdb79e>&nbsp;
 * <tr>
 * <td>139 125 107
 * <td>8b7d6b
 * <td>bisque4
 * <td bgcolor=8b7d6b>&nbsp;
 * <tr>
 * <td>255 248 220
 * <td>fff8dc
 * <td>cornsilk1
 * <td bgcolor=fff8dc>&nbsp;
 * <tr>
 * <td>238 232 205
 * <td>eee8cd
 * <td>cornsilk2
 * <td bgcolor=eee8cd>&nbsp;
 * <tr>
 * <td>205 200 177
 * <td>cdc8b1
 * <td>cornsilk3
 * <td bgcolor=cdc8b1>&nbsp;
 * <tr>
 * <td>139 136 120
 * <td>8b8878
 * <td>cornsilk4
 * <td bgcolor=8b8878>&nbsp;
 * <tr>
 * <td>255 255 240
 * <td>fffff0
 * <td>ivory1
 * <td bgcolor=fffff0>&nbsp;
 * <tr>
 * <td>238 238 224
 * <td>eeeee0
 * <td>ivory2
 * <td bgcolor=eeeee0>&nbsp;
 * <tr>
 * <td>205 205 193
 * <td>cdcdc1
 * <td>ivory3
 * <td bgcolor=cdcdc1>&nbsp;
 * <tr>
 * <td>139 139 131
 * <td>8b8b83
 * <td>ivory4
 * <td bgcolor=8b8b83>&nbsp;
 * <tr>
 * <td>240 255 240
 * <td>f0fff0
 * <td>honeydew1
 * <td bgcolor=f0fff0>&nbsp;
 * <tr>
 * <td>224 238 224
 * <td>e0eee0
 * <td>honeydew2
 * <td bgcolor=e0eee0>&nbsp;
 * <tr>
 * <td>193 205 193
 * <td>c1cdc1
 * <td>honeydew3
 * <td bgcolor=c1cdc1>&nbsp;
 * <tr>
 * <td>131 139 131
 * <td>838b83
 * <td>honeydew4
 * <td bgcolor=838b83>&nbsp;
 * <tr>
 * <td>224 238 238
 * <td>e0eeee
 * <td>azure2
 * <td bgcolor=e0eeee>&nbsp;
 * <tr>
 * <td>193 205 205
 * <td>c1cdcd
 * <td>azure3
 * <td bgcolor=c1cdcd>&nbsp;
 * <tr>
 * <td>131 139 139
 * <td>838b8b
 * <td>azure4
 * <td bgcolor=838b8b>&nbsp;
 * <tr>
 * <td> 0 0 255
 * <td>0000ff
 * <td>blue1
 * <td bgcolor=0000ff>&nbsp;
 * <tr>
 * <td> 0 0 238
 * <td>0000ee
 * <td>blue2
 * <td bgcolor=0000ee>&nbsp;
 * <tr>
 * <td> 0 0 205
 * <td>0000cd
 * <td>blue3
 * <td bgcolor=0000cd>&nbsp;
 * <tr>
 * <td> 0 0 139
 * <td>00008b
 * <td>blue4
 * <td bgcolor=00008b>&nbsp;
 * <tr>
 * <td> 0 245 255
 * <td>00f5ff
 * <td>turquoise1
 * <td bgcolor=00f5ff>&nbsp;
 * <tr>
 * <td> 0 229 238
 * <td>00e5ee
 * <td>turquoise2
 * <td bgcolor=00e5ee>&nbsp;
 * <tr>
 * <td> 0 197 205
 * <td>00c5cd
 * <td>turquoise3
 * <td bgcolor=00c5cd>&nbsp;
 * <tr>
 * <td> 0 134 139
 * <td>00868b
 * <td>turquoise4
 * <td bgcolor=00868b>&nbsp;
 * <tr>
 * <td> 0 255 255
 * <td>00ffff
 * <td>cyan1
 * <td bgcolor=00ffff>&nbsp;
 * <tr>
 * <td> 0 238 238
 * <td>00eeee
 * <td>cyan2
 * <td bgcolor=00eeee>&nbsp;
 * <tr>
 * <td> 0 205 205
 * <td>00cdcd
 * <td>cyan3
 * <td bgcolor=00cdcd>&nbsp;
 * <tr>
 * <td> 0 139 139
 * <td>008b8b
 * <td>cyan4
 * <td bgcolor=008b8b>&nbsp;
 * <tr>
 * <td>127 255 212
 * <td>7fffd4
 * <td>aquamarine1
 * <td bgcolor=7fffd4>&nbsp;
 * <tr>
 * <td>118 238 198
 * <td>76eec6
 * <td>aquamarine2
 * <td bgcolor=76eec6>&nbsp;
 * <tr>
 * <td>102 205 170
 * <td>66cdaa
 * <td>aquamarine3
 * <td bgcolor=66cdaa>&nbsp;
 * <tr>
 * <td> 69 139 116
 * <td>458b74
 * <td>aquamarine4
 * <td bgcolor=458b74>&nbsp;
 * <tr>
 * <td> 0 255 0
 * <td>00ff00
 * <td>green1
 * <td bgcolor=00ff00>&nbsp;
 * <tr>
 * <td> 0 238 0
 * <td>00ee00
 * <td>green2
 * <td bgcolor=00ee00>&nbsp;
 * <tr>
 * <td> 0 205 0
 * <td>00cd00
 * <td>green3
 * <td bgcolor=00cd00>&nbsp;
 * <tr>
 * <td> 0 139 0
 * <td>008b00
 * <td>green4
 * <td bgcolor=008b00>&nbsp;
 * <tr>
 * <td>127 255 0
 * <td>7fff00
 * <td>chartreuse1
 * <td bgcolor=7fff00>&nbsp;
 * <tr>
 * <td>118 238 0
 * <td>76ee00
 * <td>chartreuse2
 * <td bgcolor=76ee00>&nbsp;
 * <tr>
 * <td>102 205 0
 * <td>66cd00
 * <td>chartreuse3
 * <td bgcolor=66cd00>&nbsp;
 * <tr>
 * <td> 69 139 0
 * <td>458b00
 * <td>chartreuse4
 * <td bgcolor=458b00>&nbsp;
 * <tr>
 * <td>255 246 143
 * <td>fff68f
 * <td>khaki1
 * <td bgcolor=fff68f>&nbsp;
 * <tr>
 * <td>238 230 133
 * <td>eee685
 * <td>khaki2
 * <td bgcolor=eee685>&nbsp;
 * <tr>
 * <td>205 198 115
 * <td>cdc673
 * <td>khaki3
 * <td bgcolor=cdc673>&nbsp;
 * <tr>
 * <td>139 134 78
 * <td>8b864e
 * <td>khaki4
 * <td bgcolor=8b864e>&nbsp;
 * <tr>
 * <td>255 255 0
 * <td>ffff00
 * <td>yellow1
 * <td bgcolor=ffff00>&nbsp;
 * <tr>
 * <td>238 238 0
 * <td>eeee00
 * <td>yellow2
 * <td bgcolor=eeee00>&nbsp;
 * <tr>
 * <td>205 205 0
 * <td>cdcd00
 * <td>yellow3
 * <td bgcolor=cdcd00>&nbsp;
 * <tr>
 * <td>139 139 0
 * <td>8b8b00
 * <td>yellow4
 * <td bgcolor=8b8b00>&nbsp;
 * <tr>
 * <td>255 215 0
 * <td>ffd700
 * <td>gold1
 * <td bgcolor=ffd700>&nbsp;
 * <tr>
 * <td>238 201 0
 * <td>eec900
 * <td>gold2
 * <td bgcolor=eec900>&nbsp;
 * <tr>
 * <td>205 173 0
 * <td>cdad00
 * <td>gold3
 * <td bgcolor=cdad00>&nbsp;
 * <tr>
 * <td>139 117 0
 * <td>8b7500
 * <td>gold4
 * <td bgcolor=8b7500>&nbsp;
 * <tr>
 * <td>255 193 37
 * <td>ffc125
 * <td>goldenrod1
 * <td bgcolor=ffc125>&nbsp;
 * <tr>
 * <td>238 180 34
 * <td>eeb422
 * <td>goldenrod2
 * <td bgcolor=eeb422>&nbsp;
 * <tr>
 * <td>205 155 29
 * <td>cd9b1d
 * <td>goldenrod3
 * <td bgcolor=cd9b1d>&nbsp;
 * <tr>
 * <td>139 105 20
 * <td>8b6914
 * <td>goldenrod4
 * <td bgcolor=8b6914>&nbsp;
 * <tr>
 * <td>255 130 71
 * <td>ff8247
 * <td>sienna1
 * <td bgcolor=ff8247>&nbsp;
 * <tr>
 * <td>238 121 66
 * <td>ee7942
 * <td>sienna2
 * <td bgcolor=ee7942>&nbsp;
 * <tr>
 * <td>205 104 57
 * <td>cd6839
 * <td>sienna3
 * <td bgcolor=cd6839>&nbsp;
 * <tr>
 * <td>139 71 38
 * <td>8b4726
 * <td>sienna4
 * <td bgcolor=8b4726>&nbsp;
 * <tr>
 * <td>255 211 155
 * <td>ffd39b
 * <td>burlywood1
 * <td bgcolor=ffd39b>&nbsp;
 * <tr>
 * <td>238 197 145
 * <td>eec591
 * <td>burlywood2
 * <td bgcolor=eec591>&nbsp;
 * <tr>
 * <td>205 170 125
 * <td>cdaa7d
 * <td>burlywood3
 * <td bgcolor=cdaa7d>&nbsp;
 * <tr>
 * <td>139 115 85
 * <td>8b7355
 * <td>burlywood4
 * <td bgcolor=8b7355>&nbsp;
 * <tr>
 * <td>255 231 186
 * <td>ffe7ba
 * <td>wheat1
 * <td bgcolor=ffe7ba>&nbsp;
 * <tr>
 * <td>238 216 174
 * <td>eed8ae
 * <td>wheat2
 * <td bgcolor=eed8ae>&nbsp;
 * <tr>
 * <td>205 186 150
 * <td>cdba96
 * <td>wheat3
 * <td bgcolor=cdba96>&nbsp;
 * <tr>
 * <td>139 126 102
 * <td>8b7e66
 * <td>wheat4
 * <td bgcolor=8b7e66>&nbsp;
 * <tr>
 * <td>255 165 79
 * <td>ffa54f
 * <td>tan1
 * <td bgcolor=ffa54f>&nbsp;
 * <tr>
 * <td>238 154 73
 * <td>ee9a49
 * <td>tan2
 * <td bgcolor=ee9a49>&nbsp;
 * <tr>
 * <td>205 133 63
 * <td>cd853f
 * <td>tan3
 * <td bgcolor=cd853f>&nbsp;
 * <tr>
 * <td>139 90 43
 * <td>8b5a2b
 * <td>tan4
 * <td bgcolor=8b5a2b>&nbsp;
 * <tr>
 * <td>255 127 36
 * <td>ff7f24
 * <td>chocolate1
 * <td bgcolor=ff7f24>&nbsp;
 * <tr>
 * <td>238 118 33
 * <td>ee7621
 * <td>chocolate2
 * <td bgcolor=ee7621>&nbsp;
 * <tr>
 * <td>205 102 29
 * <td>cd661d
 * <td>chocolate3
 * <td bgcolor=cd661d>&nbsp;
 * <tr>
 * <td>139 69 19
 * <td>8b4513
 * <td>chocolate4
 * <td bgcolor=8b4513>&nbsp;
 * <tr>
 * <td>255 48 48
 * <td>ff3030
 * <td>firebrick1
 * <td bgcolor=ff3030>&nbsp;
 * <tr>
 * <td>238 44 44
 * <td>ee2c2c
 * <td>firebrick2
 * <td bgcolor=ee2c2c>&nbsp;
 * <tr>
 * <td>205 38 38
 * <td>cd2626
 * <td>firebrick3
 * <td bgcolor=cd2626>&nbsp;
 * <tr>
 * <td>139 26 26
 * <td>8b1a1a
 * <td>firebrick4
 * <td bgcolor=8b1a1a>&nbsp;
 * <tr>
 * <td>255 64 64
 * <td>ff4040
 * <td>brown1
 * <td bgcolor=ff4040>&nbsp;
 * <tr>
 * <td>238 59 59
 * <td>ee3b3b
 * <td>brown2
 * <td bgcolor=ee3b3b>&nbsp;
 * <tr>
 * <td>205 51 51
 * <td>cd3333
 * <td>brown3
 * <td bgcolor=cd3333>&nbsp;
 * <tr>
 * <td>139 35 35
 * <td>8b2323
 * <td>brown4
 * <td bgcolor=8b2323>&nbsp;
 * <tr>
 * <td>255 140 105
 * <td>ff8c69
 * <td>salmon1
 * <td bgcolor=ff8c69>&nbsp;
 * <tr>
 * <td>238 130 98
 * <td>ee8262
 * <td>salmon2
 * <td bgcolor=ee8262>&nbsp;
 * <tr>
 * <td>205 112 84
 * <td>cd7054
 * <td>salmon3
 * <td bgcolor=cd7054>&nbsp;
 * <tr>
 * <td>139 76 57
 * <td>8b4c39
 * <td>salmon4
 * <td bgcolor=8b4c39>&nbsp;
 * <tr>
 * <td>255 165 0
 * <td>ffa500
 * <td>orange1
 * <td bgcolor=ffa500>&nbsp;
 * <tr>
 * <td>238 154 0
 * <td>ee9a00
 * <td>orange2
 * <td bgcolor=ee9a00>&nbsp;
 * <tr>
 * <td>205 133 0
 * <td>cd8500
 * <td>orange3
 * <td bgcolor=cd8500>&nbsp;
 * <tr>
 * <td>139 90 0
 * <td>8b5a00
 * <td>orange4
 * <td bgcolor=8b5a00>&nbsp;
 * <tr>
 * <td>238 106 80
 * <td>ee6a50
 * <td>coral2
 * <td bgcolor=ee6a50>&nbsp;
 * <tr>
 * <td>205 91 69
 * <td>cd5b45
 * <td>coral3
 * <td bgcolor=cd5b45>&nbsp;
 * <tr>
 * <td>139 62 47
 * <td>8b3e2f
 * <td>coral4
 * <td bgcolor=8b3e2f>&nbsp;
 * <tr>
 * <td>255 99 71
 * <td>ff6347
 * <td>tomato1
 * <td bgcolor=ff6347>&nbsp;
 * <tr>
 * <td>238 92 66
 * <td>ee5c42
 * <td>tomato2
 * <td bgcolor=ee5c42>&nbsp;
 * <tr>
 * <td>205 79 57
 * <td>cd4f39
 * <td>tomato3
 * <td bgcolor=cd4f39>&nbsp;
 * <tr>
 * <td>139 54 38
 * <td>8b3626
 * <td>tomato4
 * <td bgcolor=8b3626>&nbsp;
 * <tr>
 * <td>255 0 0
 * <td>ff0000
 * <td>red1
 * <td bgcolor=ff0000>&nbsp;
 * <tr>
 * <td>238 0 0
 * <td>ee0000
 * <td>red2
 * <td bgcolor=ee0000>&nbsp;
 * <tr>
 * <td>205 0 0
 * <td>cd0000
 * <td>red3
 * <td bgcolor=cd0000>&nbsp;
 * <tr>
 * <td>139 0 0
 * <td>8b0000
 * <td>red4
 * <td bgcolor=8b0000>&nbsp;
 * <tr>
 * <td>255 181 197
 * <td>ffb5c5
 * <td>pink1
 * <td bgcolor=ffb5c5>&nbsp;
 * <tr>
 * <td>238 169 184
 * <td>eea9b8
 * <td>pink2
 * <td bgcolor=eea9b8>&nbsp;
 * <tr>
 * <td>205 145 158
 * <td>cd919e
 * <td>pink3
 * <td bgcolor=cd919e>&nbsp;
 * <tr>
 * <td>139 99 108
 * <td>8b636c
 * <td>pink4
 * <td bgcolor=8b636c>&nbsp;
 * <tr>
 * <td>238 48 167
 * <td>ee30a7
 * <td>maroon2
 * <td bgcolor=ee30a7>&nbsp;
 * <tr>
 * <td>205 41 144
 * <td>cd2990
 * <td>maroon3
 * <td bgcolor=cd2990>&nbsp;
 * <tr>
 * <td>139 28 98
 * <td>8b1c62
 * <td>maroon4
 * <td bgcolor=8b1c62>&nbsp;
 * <tr>
 * <td>255 0 255
 * <td>ff00ff
 * <td>magenta1
 * <td bgcolor=ff00ff>&nbsp;
 * <tr>
 * <td>238 0 238
 * <td>ee00ee
 * <td>magenta2
 * <td bgcolor=ee00ee>&nbsp;
 * <tr>
 * <td>205 0 205
 * <td>cd00cd
 * <td>magenta3
 * <td bgcolor=cd00cd>&nbsp;
 * <tr>
 * <td>139 0 139
 * <td>8b008b
 * <td>magenta4
 * <td bgcolor=8b008b>&nbsp;
 * <tr>
 * <td>255 131 250
 * <td>ff83fa
 * <td>orchid1
 * <td bgcolor=ff83fa>&nbsp;
 * <tr>
 * <td>238 122 233
 * <td>ee7ae9
 * <td>orchid2
 * <td bgcolor=ee7ae9>&nbsp;
 * <tr>
 * <td>205 105 201
 * <td>cd69c9
 * <td>orchid3
 * <td bgcolor=cd69c9>&nbsp;
 * <tr>
 * <td>139 71 137
 * <td>8b4789
 * <td>orchid4
 * <td bgcolor=8b4789>&nbsp;
 * <tr>
 * <td>255 187 255
 * <td>ffbbff
 * <td>plum1
 * <td bgcolor=ffbbff>&nbsp;
 * <tr>
 * <td>238 174 238
 * <td>eeaeee
 * <td>plum2
 * <td bgcolor=eeaeee>&nbsp;
 * <tr>
 * <td>205 150 205
 * <td>cd96cd
 * <td>plum3
 * <td bgcolor=cd96cd>&nbsp;
 * <tr>
 * <td>139 102 139
 * <td>8b668b
 * <td>plum4
 * <td bgcolor=8b668b>&nbsp;
 * <tr>
 * <td>155 48 255
 * <td>9b30ff
 * <td>purple1
 * <td bgcolor=9b30ff>&nbsp;
 * <tr>
 * <td>145 44 238
 * <td>912cee
 * <td>purple2
 * <td bgcolor=912cee>&nbsp;
 * <tr>
 * <td>125 38 205
 * <td>7d26cd
 * <td>purple3
 * <td bgcolor=7d26cd>&nbsp;
 * <tr>
 * <td> 85 26 139
 * <td>551a8b
 * <td>purple4
 * <td bgcolor=551a8b>&nbsp;
 * <tr>
 * <td>255 225 255
 * <td>ffe1ff
 * <td>thistle1
 * <td bgcolor=ffe1ff>&nbsp;
 * <tr>
 * <td>238 210 238
 * <td>eed2ee
 * <td>thistle2
 * <td bgcolor=eed2ee>&nbsp;
 * <tr>
 * <td>205 181 205
 * <td>cdb5cd
 * <td>thistle3
 * <td bgcolor=cdb5cd>&nbsp;
 * <tr>
 * <td> 0 0 0
 * <td>000000
 * <td>grey0
 * <td bgcolor=000000>&nbsp;
 * <tr>
 * <td> 3 3 3
 * <td>030303
 * <td>grey1
 * <td bgcolor=030303>&nbsp;
 * <tr>
 * <td> 5 5 5
 * <td>050505
 * <td>grey2
 * <td bgcolor=050505>&nbsp;
 * <tr>
 * <td> 8 8 8
 * <td>080808
 * <td>grey3
 * <td bgcolor=080808>&nbsp;
 * <tr>
 * <td> 10 10 10
 * <td>0a0a0a
 * <td> grey4
 * <td bgcolor=0a0a0a>&nbsp;
 * <tr>
 * <td> 13 13 13
 * <td>0d0d0d
 * <td> grey5
 * <td bgcolor=0d0d0d>&nbsp;
 * <tr>
 * <td> 15 15 15
 * <td>0f0f0f
 * <td> grey6
 * <td bgcolor=0f0f0f>&nbsp;
 * <tr>
 * <td> 18 18 18
 * <td>121212
 * <td> grey7
 * <td bgcolor=121212>&nbsp;
 * <tr>
 * <td> 20 20 20
 * <td>141414
 * <td> grey8
 * <td bgcolor=141414>&nbsp;
 * <tr>
 * <td> 23 23 23
 * <td>171717
 * <td> grey9
 * <td bgcolor=171717>&nbsp;
 * <tr>
 * <td> 26 26 26
 * <td>1a1a1a
 * <td> grey10
 * <td bgcolor=1a1a1a>&nbsp;
 * <tr>
 * <td> 28 28 28
 * <td>1c1c1c
 * <td> grey11
 * <td bgcolor=1c1c1c>&nbsp;
 * <tr>
 * <td> 31 31 31
 * <td>1f1f1f
 * <td> grey12
 * <td bgcolor=1f1f1f>&nbsp;
 * <tr>
 * <td> 33 33 33
 * <td>212121
 * <td> grey13
 * <td bgcolor=212121>&nbsp;
 * <tr>
 * <td> 36 36 36
 * <td>242424
 * <td> grey14
 * <td bgcolor=242424>&nbsp;
 * <tr>
 * <td> 38 38 38
 * <td>262626
 * <td> grey15
 * <td bgcolor=262626>&nbsp;
 * <tr>
 * <td> 41 41 41
 * <td>292929
 * <td> grey16
 * <td bgcolor=292929>&nbsp;
 * <tr>
 * <td> 43 43 43
 * <td>2b2b2b
 * <td> grey17
 * <td bgcolor=2b2b2b>&nbsp;
 * <tr>
 * <td> 46 46 46
 * <td>2e2e2e
 * <td> grey18
 * <td bgcolor=2e2e2e>&nbsp;
 * <tr>
 * <td> 48 48 48
 * <td>303030
 * <td> grey19
 * <td bgcolor=303030>&nbsp;
 * <tr>
 * <td> 51 51 51
 * <td>333333
 * <td> grey20
 * <td bgcolor=333333>&nbsp;
 * <tr>
 * <td> 54 54 54
 * <td>363636
 * <td> grey21
 * <td bgcolor=363636>&nbsp;
 * <tr>
 * <td> 56 56 56
 * <td>383838
 * <td> grey22
 * <td bgcolor=383838>&nbsp;
 * <tr>
 * <td> 59 59 59
 * <td>3b3b3b
 * <td> grey23
 * <td bgcolor=3b3b3b>&nbsp;
 * <tr>
 * <td> 61 61 61
 * <td>3d3d3d
 * <td> grey24
 * <td bgcolor=3d3d3d>&nbsp;
 * <tr>
 * <td> 64 64 64
 * <td>404040
 * <td> grey25
 * <td bgcolor=404040>&nbsp;
 * <tr>
 * <td> 66 66 66
 * <td>424242
 * <td> grey26
 * <td bgcolor=424242>&nbsp;
 * <tr>
 * <td> 69 69 69
 * <td>454545
 * <td> grey27
 * <td bgcolor=454545>&nbsp;
 * <tr>
 * <td> 71 71 71
 * <td>474747
 * <td> grey28
 * <td bgcolor=474747>&nbsp;
 * <tr>
 * <td> 74 74 74
 * <td>4a4a4a
 * <td> grey29
 * <td bgcolor=4a4a4a>&nbsp;
 * <tr>
 * <td> 77 77 77
 * <td>4d4d4d
 * <td> grey30
 * <td bgcolor=4d4d4d>&nbsp;
 * <tr>
 * <td> 79 79 79
 * <td>4f4f4f
 * <td> grey31
 * <td bgcolor=4f4f4f>&nbsp;
 * <tr>
 * <td> 82 82 82
 * <td>525252
 * <td> grey32
 * <td bgcolor=525252>&nbsp;
 * <tr>
 * <td> 84 84 84
 * <td>545454
 * <td> grey33
 * <td bgcolor=545454>&nbsp;
 * <tr>
 * <td> 87 87 87
 * <td>575757
 * <td> grey34
 * <td bgcolor=575757>&nbsp;
 * <tr>
 * <td> 89 89 89
 * <td>595959
 * <td> grey35
 * <td bgcolor=595959>&nbsp;
 * <tr>
 * <td> 92 92 92
 * <td>5c5c5c
 * <td> grey36
 * <td bgcolor=5c5c5c>&nbsp;
 * <tr>
 * <td> 94 94 94
 * <td>5e5e5e
 * <td> grey37
 * <td bgcolor=5e5e5e>&nbsp;
 * <tr>
 * <td> 97 97 97
 * <td>616161
 * <td> grey38
 * <td bgcolor=616161>&nbsp;
 * <tr>
 * <td> 99 99 99
 * <td>636363
 * <td> grey39
 * <td bgcolor=636363>&nbsp;
 * <tr>
 * <td>102 102 102
 * <td>666666
 * <td> grey40
 * <td bgcolor=666666>&nbsp;
 * <tr>
 * <td>105 105 105
 * <td>696969
 * <td> grey41
 * <td bgcolor=696969>&nbsp;
 * <tr>
 * <td>107 107 107
 * <td>6b6b6b
 * <td> grey42
 * <td bgcolor=6b6b6b>&nbsp;
 * <tr>
 * <td>110 110 110
 * <td>6e6e6e
 * <td> grey43
 * <td bgcolor=6e6e6e>&nbsp;
 * <tr>
 * <td>112 112 112
 * <td>707070
 * <td> grey44
 * <td bgcolor=707070>&nbsp;
 * <tr>
 * <td>115 115 115
 * <td>737373
 * <td> grey45
 * <td bgcolor=737373>&nbsp;
 * <tr>
 * <td>117 117 117
 * <td>757575
 * <td> grey46
 * <td bgcolor=757575>&nbsp;
 * <tr>
 * <td>120 120 120
 * <td>787878
 * <td> grey47
 * <td bgcolor=787878>&nbsp;
 * <tr>
 * <td>122 122 122
 * <td>7a7a7a
 * <td> grey48
 * <td bgcolor=7a7a7a>&nbsp;
 * <tr>
 * <td>125 125 125
 * <td>7d7d7d
 * <td> grey49
 * <td bgcolor=7d7d7d>&nbsp;
 * <tr>
 * <td>127 127 127
 * <td>7f7f7f
 * <td> grey50
 * <td bgcolor=7f7f7f>&nbsp;
 * <tr>
 * <td>130 130 130
 * <td>828282
 * <td> grey51
 * <td bgcolor=828282>&nbsp;
 * <tr>
 * <td>133 133 133
 * <td>858585
 * <td> grey52
 * <td bgcolor=858585>&nbsp;
 * <tr>
 * <td>135 135 135
 * <td>878787
 * <td> grey53
 * <td bgcolor=878787>&nbsp;
 * <tr>
 * <td>138 138 138
 * <td>8a8a8a
 * <td> grey54
 * <td bgcolor=8a8a8a>&nbsp;
 * <tr>
 * <td>140 140 140
 * <td>8c8c8c
 * <td> grey55
 * <td bgcolor=8c8c8c>&nbsp;
 * <tr>
 * <td>143 143 143
 * <td>8f8f8f
 * <td> grey56
 * <td bgcolor=8f8f8f>&nbsp;
 * <tr>
 * <td>145 145 145
 * <td>919191
 * <td> grey57
 * <td bgcolor=919191>&nbsp;
 * <tr>
 * <td>148 148 148
 * <td>949494
 * <td> grey58
 * <td bgcolor=949494>&nbsp;
 * <tr>
 * <td>150 150 150
 * <td>969696
 * <td> grey59
 * <td bgcolor=969696>&nbsp;
 * <tr>
 * <td>153 153 153
 * <td>999999
 * <td> grey60
 * <td bgcolor=999999>&nbsp;
 * <tr>
 * <td>156 156 156
 * <td>9c9c9c
 * <td> grey61
 * <td bgcolor=9c9c9c>&nbsp;
 * <tr>
 * <td>158 158 158
 * <td>9e9e9e
 * <td> grey62
 * <td bgcolor=9e9e9e>&nbsp;
 * <tr>
 * <td>161 161 161
 * <td>a1a1a1
 * <td> grey63
 * <td bgcolor=a1a1a1>&nbsp;
 * <tr>
 * <td>163 163 163
 * <td>a3a3a3
 * <td> grey64
 * <td bgcolor=a3a3a3>&nbsp;
 * <tr>
 * <td>166 166 166
 * <td>a6a6a6
 * <td> grey65
 * <td bgcolor=a6a6a6>&nbsp;
 * <tr>
 * <td>168 168 168
 * <td>a8a8a8
 * <td> grey66
 * <td bgcolor=a8a8a8>&nbsp;
 * <tr>
 * <td>171 171 171
 * <td>ababab
 * <td> grey67
 * <td bgcolor=ababab>&nbsp;
 * <tr>
 * <td>173 173 173
 * <td>adadad
 * <td> grey68
 * <td bgcolor=adadad>&nbsp;
 * <tr>
 * <td>176 176 176
 * <td>b0b0b0
 * <td> grey69
 * <td bgcolor=b0b0b0>&nbsp;
 * <tr>
 * <td>179 179 179
 * <td>b3b3b3
 * <td> grey70
 * <td bgcolor=b3b3b3>&nbsp;
 * <tr>
 * <td>181 181 181
 * <td>b5b5b5
 * <td> grey71
 * <td bgcolor=b5b5b5>&nbsp;
 * <tr>
 * <td>184 184 184
 * <td>b8b8b8
 * <td> grey72
 * <td bgcolor=b8b8b8>&nbsp;
 * <tr>
 * <td>186 186 186
 * <td>bababa
 * <td> grey73
 * <td bgcolor=bababa>&nbsp;
 * <tr>
 * <td>189 189 189
 * <td>bdbdbd
 * <td> grey74
 * <td bgcolor=bdbdbd>&nbsp;
 * <tr>
 * <td>191 191 191
 * <td>bfbfbf
 * <td> grey75
 * <td bgcolor=bfbfbf>&nbsp;
 * <tr>
 * <td>194 194 194
 * <td>c2c2c2
 * <td> grey76
 * <td bgcolor=c2c2c2>&nbsp;
 * <tr>
 * <td>196 196 196
 * <td>c4c4c4
 * <td> grey77
 * <td bgcolor=c4c4c4>&nbsp;
 * <tr>
 * <td>199 199 199
 * <td>c7c7c7
 * <td> grey78
 * <td bgcolor=c7c7c7>&nbsp;
 * <tr>
 * <td>201 201 201
 * <td>c9c9c9
 * <td> grey79
 * <td bgcolor=c9c9c9>&nbsp;
 * <tr>
 * <td>204 204 204
 * <td>cccccc
 * <td> grey80
 * <td bgcolor=cccccc>&nbsp;
 * <tr>
 * <td>207 207 207
 * <td>cfcfcf
 * <td> grey81
 * <td bgcolor=cfcfcf>&nbsp;
 * <tr>
 * <td>209 209 209
 * <td>d1d1d1
 * <td> grey82
 * <td bgcolor=d1d1d1>&nbsp;
 * <tr>
 * <td>212 212 212
 * <td>d4d4d4
 * <td> grey83
 * <td bgcolor=d4d4d4>&nbsp;
 * <tr>
 * <td>214 214 214
 * <td>d6d6d6
 * <td> grey84
 * <td bgcolor=d6d6d6>&nbsp;
 * <tr>
 * <td>217 217 217
 * <td>d9d9d9
 * <td> grey85
 * <td bgcolor=d9d9d9>&nbsp;
 * <tr>
 * <td>219 219 219
 * <td>dbdbdb
 * <td> grey86
 * <td bgcolor=dbdbdb>&nbsp;
 * <tr>
 * <td>222 222 222
 * <td>dedede
 * <td> grey87
 * <td bgcolor=dedede>&nbsp;
 * <tr>
 * <td>224 224 224
 * <td>e0e0e0
 * <td> grey88
 * <td bgcolor=e0e0e0>&nbsp;
 * <tr>
 * <td>227 227 227
 * <td>e3e3e3
 * <td> grey89
 * <td bgcolor=e3e3e3>&nbsp;
 * <tr>
 * <td>229 229 229
 * <td>e5e5e5
 * <td> grey90
 * <td bgcolor=e5e5e5>&nbsp;
 * <tr>
 * <td>232 232 232
 * <td>e8e8e8
 * <td> grey91
 * <td bgcolor=e8e8e8>&nbsp;
 * <tr>
 * <td>235 235 235
 * <td>ebebeb
 * <td> grey92
 * <td bgcolor=ebebeb>&nbsp;
 * <tr>
 * <td>237 237 237
 * <td>ededed
 * <td> grey93
 * <td bgcolor=ededed>&nbsp;
 * <tr>
 * <td>240 240 240
 * <td>f0f0f0
 * <td> grey94
 * <td bgcolor=f0f0f0>&nbsp;
 * <tr>
 * <td>242 242 242
 * <td>f2f2f2
 * <td> grey95
 * <td bgcolor=f2f2f2>&nbsp;
 * <tr>
 * <td>245 245 245
 * <td>f5f5f5
 * <td> grey96
 * <td bgcolor=f5f5f5>&nbsp;
 * <tr>
 * <td>247 247 247
 * <td>f7f7f7
 * <td> grey97
 * <td bgcolor=f7f7f7>&nbsp;
 * <tr>
 * <td>250 250 250
 * <td>fafafa
 * <td> grey98
 * <td bgcolor=fafafa>&nbsp;
 * <tr>
 * <td>252 252 252
 * <td>fcfcfc
 * <td> grey99
 * <td bgcolor=fcfcfc>&nbsp;
 * <tr>
 * <td>255 255 255
 * <td>ffffff
 * <td> grey100
 * <td bgcolor=ffffff>&nbsp; </table>
 *
 * @author The ProActive Team
 * @since ProActive 3.2
 */
public class Colors {

    /** the color "snow" */
    public static final Color SNOW;

    /** the color "ghost_white" */
    public static final Color GHOST_WHITE;

    /** the color "white_smoke" */
    public static final Color WHITE_SMOKE;

    /** the color "gainsboro" */
    public static final Color GAINSBORO;

    /** the color "floral_white" */
    public static final Color FLORAL_WHITE;

    /** the color "old_lace" */
    public static final Color OLD_LACE;

    /** the color "linen" */
    public static final Color LINEN;

    /** the color "antique_white" */
    public static final Color ANTIQUE_WHITE;

    /** the color "papaya_whip" */
    public static final Color PAPAYA_WHIP;

    /** the color "blanched_almond" */
    public static final Color BLANCHED_ALMOND;

    /** the color "bisque" */
    public static final Color BISQUE;

    /** the color "peach_puff" */
    public static final Color PEACH_PUFF;

    /** the color "navajo_white" */
    public static final Color NAVAJO_WHITE;

    /** the color "moccasin" */
    public static final Color MOCCASIN;

    /** the color "cornsilk" */
    public static final Color CORNSILK;

    /** the color "ivory" */
    public static final Color IVORY;

    /** the color "lemon_chiffon" */
    public static final Color LEMON_CHIFFON;

    /** the color "seashell" */
    public static final Color SEASHELL;

    /** the color "honeydew" */
    public static final Color HONEYDEW;

    /** the color "mint_cream" */
    public static final Color MINT_CREAM;

    /** the color "azure" */
    public static final Color AZURE;

    /** the color "alice_blue" */
    public static final Color ALICE_BLUE;

    /** the color "lavender" */
    public static final Color LAVENDER;

    /** the color "lavender_blush" */
    public static final Color LAVENDER_BLUSH;

    /** the color "misty_rose" */
    public static final Color MISTY_ROSE;

    /** the color "white" */
    public static final Color WHITE;

    /** the color "black" */
    public static final Color BLACK;

    /** the color "dark_slate_grey" */
    public static final Color DARK_SLATE_GREY;

    /** the color "dim_grey" */
    public static final Color DIM_GREY;

    /** the color "slate_grey" */
    public static final Color SLATE_GREY;

    /** the color "light_slate_grey" */
    public static final Color LIGHT_SLATE_GREY;

    /** the color "light_grey" */
    public static final Color LIGHT_GREY;

    /** the color "midnight_blue" */
    public static final Color MIDNIGHT_BLUE;

    /** the color "navy" */
    public static final Color NAVY;

    /** the color "navy_blue" */
    public static final Color NAVY_BLUE;

    /** the color "cornflower_blue" */
    public static final Color CORNFLOWER_BLUE;

    /** the color "dark_slate_blue" */
    public static final Color DARK_SLATE_BLUE;

    /** the color "slate_blue" */
    public static final Color SLATE_BLUE;

    /** the color "medium_slate_blue" */
    public static final Color MEDIUM_SLATE_BLUE;

    /** the color "light_slate_blue" */
    public static final Color LIGHT_SLATE_BLUE;

    /** the color "medium_blue" */
    public static final Color MEDIUM_BLUE;

    /** the color "royal_blue" */
    public static final Color ROYAL_BLUE;

    /** the color "blue" */
    public static final Color BLUE;

    /** the color "dodger_blue" */
    public static final Color DODGER_BLUE;

    /** the color "deep_sky_blue" */
    public static final Color DEEP_SKY_BLUE;

    /** the color "sky_blue" */
    public static final Color SKY_BLUE;

    /** the color "light_sky_blue" */
    public static final Color LIGHT_SKY_BLUE;

    /** the color "light_steel_blue" */
    public static final Color LIGHT_STEEL_BLUE;

    /** the color "light_blue" */
    public static final Color LIGHT_BLUE;

    /** the color "powder_blue" */
    public static final Color POWDER_BLUE;

    /** the color "dark_turquoise" */
    public static final Color DARK_TURQUOISE;

    /** the color "medium_turquoise" */
    public static final Color MEDIUM_TURQUOISE;

    /** the color "turquoise" */
    public static final Color TURQUOISE;

    /** the color "cyan" */
    public static final Color CYAN;

    /** the color "light_cyan" */
    public static final Color LIGHT_CYAN;

    /** the color "cadet_blue" */
    public static final Color CADET_BLUE;

    /** the color "medium_aquamarine" */
    public static final Color MEDIUM_AQUAMARINE;

    /** the color "aquamarine" */
    public static final Color AQUAMARINE;

    /** the color "dark_green" */
    public static final Color DARK_GREEN;

    /** the color "dark_olive_green" */
    public static final Color DARK_OLIVE_GREEN;

    /** the color "dark_sea_green" */
    public static final Color DARK_SEA_GREEN;

    /** the color "sea_green" */
    public static final Color SEA_GREEN;

    /** the color "medium_sea_green" */
    public static final Color MEDIUM_SEA_GREEN;

    /** the color "light_sea_green" */
    public static final Color LIGHT_SEA_GREEN;

    /** the color "pale_green" */
    public static final Color PALE_GREEN;

    /** the color "spring_green" */
    public static final Color SPRING_GREEN;

    /** the color "lawn_green" */
    public static final Color LAWN_GREEN;

    /** the color "green" */
    public static final Color GREEN;

    /** the color "chartreuse" */
    public static final Color CHARTREUSE;

    /** the color "medium_spring_green" */
    public static final Color MEDIUM_SPRING_GREEN;

    /** the color "green_yellow" */
    public static final Color GREEN_YELLOW;

    /** the color "lime_green" */
    public static final Color LIME_GREEN;

    /** the color "yellow_green" */
    public static final Color YELLOW_GREEN;

    /** the color "forest_green" */
    public static final Color FOREST_GREEN;

    /** the color "olive_drab" */
    public static final Color OLIVE_DRAB;

    /** the color "dark_khaki" */
    public static final Color DARK_KHAKI;

    /** the color "khaki" */
    public static final Color KHAKI;

    /** the color "pale_goldenrod" */
    public static final Color PALE_GOLDENROD;

    /** the color "light_yellow" */
    public static final Color LIGHT_YELLOW;

    /** the color "yellow" */
    public static final Color YELLOW;

    /** the color "gold" */
    public static final Color GOLD;

    /** the color "light_goldenrod" */
    public static final Color LIGHT_GOLDENROD;

    /** the color "goldenrod" */
    public static final Color GOLDENROD;

    /** the color "dark_goldenrod" */
    public static final Color DARK_GOLDENROD;

    /** the color "rosy_brown" */
    public static final Color ROSY_BROWN;

    /** the color "saddle_brown" */
    public static final Color SADDLE_BROWN;

    /** the color "sienna" */
    public static final Color SIENNA;

    /** the color "peru" */
    public static final Color PERU;

    /** the color "burlywood" */
    public static final Color BURLYWOOD;

    /** the color "beige" */
    public static final Color BEIGE;

    /** the color "wheat" */
    public static final Color WHEAT;

    /** the color "sandy_brown" */
    public static final Color SANDY_BROWN;

    /** the color "tan" */
    public static final Color TAN;

    /** the color "chocolate" */
    public static final Color CHOCOLATE;

    /** the color "firebrick" */
    public static final Color FIREBRICK;

    /** the color "brown" */
    public static final Color BROWN;

    /** the color "dark_salmon" */
    public static final Color DARK_SALMON;

    /** the color "salmon" */
    public static final Color SALMON;

    /** the color "light_salmon" */
    public static final Color LIGHT_SALMON;

    /** the color "orange" */
    public static final Color ORANGE;

    /** the color "dark_orange" */
    public static final Color DARK_ORANGE;

    /** the color "coral" */
    public static final Color CORAL;

    /** the color "light_coral" */
    public static final Color LIGHT_CORAL;

    /** the color "tomato" */
    public static final Color TOMATO;

    /** the color "orange_red" */
    public static final Color ORANGE_RED;

    /** the color "red" */
    public static final Color RED;

    /** the color "hot_pink" */
    public static final Color HOT_PINK;

    /** the color "deep_pink" */
    public static final Color DEEP_PINK;

    /** the color "pink" */
    public static final Color PINK;

    /** the color "pale_violet_red" */
    public static final Color PALE_VIOLET_RED;

    /** the color "maroon" */
    public static final Color MAROON;

    /** the color "medium_violet_red" */
    public static final Color MEDIUM_VIOLET_RED;

    /** the color "violet_red" */
    public static final Color VIOLET_RED;

    /** the color "magenta" */
    public static final Color MAGENTA;

    /** the color "violet" */
    public static final Color VIOLET;

    /** the color "plum" */
    public static final Color PLUM;

    /** the color "orchid" */
    public static final Color ORCHID;

    /** the color "medium_orchid" */
    public static final Color MEDIUM_ORCHID;

    /** the color "dark_orchid" */
    public static final Color DARK_ORCHID;

    /** the color "dark_violet" */
    public static final Color DARK_VIOLET;

    /** the color "purple" */
    public static final Color PURPLE;

    /** the color "medium_purple" */
    public static final Color MEDIUM_PURPLE;

    /** the color "thistle" */
    public static final Color THISTLE;

    /** the color "snow1" */
    public static final Color SNOW1;

    /** the color "snow2" */
    public static final Color SNOW2;

    /** the color "snow3" */
    public static final Color SNOW3;

    /** the color "snow4" */
    public static final Color SNOW4;

    /** the color "seashell1" */
    public static final Color SEASHELL1;

    /** the color "seashell2" */
    public static final Color SEASHELL2;

    /** the color "seashell3" */
    public static final Color SEASHELL3;

    /** the color "seashell4" */
    public static final Color SEASHELL4;

    /** the color "bisque2" */
    public static final Color BISQUE2;

    /** the color "bisque3" */
    public static final Color BISQUE3;

    /** the color "bisque4" */
    public static final Color BISQUE4;

    /** the color "cornsilk1" */
    public static final Color CORNSILK1;

    /** the color "cornsilk2" */
    public static final Color CORNSILK2;

    /** the color "cornsilk3" */
    public static final Color CORNSILK3;

    /** the color "cornsilk4" */
    public static final Color CORNSILK4;

    /** the color "ivory1" */
    public static final Color IVORY1;

    /** the color "ivory2" */
    public static final Color IVORY2;

    /** the color "ivory3" */
    public static final Color IVORY3;

    /** the color "ivory4" */
    public static final Color IVORY4;

    /** the color "honeydew1" */
    public static final Color HONEYDEW1;

    /** the color "honeydew2" */
    public static final Color HONEYDEW2;

    /** the color "honeydew3" */
    public static final Color HONEYDEW3;

    /** the color "honeydew4" */
    public static final Color HONEYDEW4;

    /** the color "azure2" */
    public static final Color AZURE2;

    /** the color "azure3" */
    public static final Color AZURE3;

    /** the color "azure4" */
    public static final Color AZURE4;

    /** the color "blue1" */
    public static final Color BLUE1;

    /** the color "blue2" */
    public static final Color BLUE2;

    /** the color "blue3" */
    public static final Color BLUE3;

    /** the color "blue4" */
    public static final Color BLUE4;

    /** the color "turquoise1" */
    public static final Color TURQUOISE1;

    /** the color "turquoise2" */
    public static final Color TURQUOISE2;

    /** the color "turquoise3" */
    public static final Color TURQUOISE3;

    /** the color "turquoise4" */
    public static final Color TURQUOISE4;

    /** the color "cyan1" */
    public static final Color CYAN1;

    /** the color "cyan2" */
    public static final Color CYAN2;

    /** the color "cyan3" */
    public static final Color CYAN3;

    /** the color "cyan4" */
    public static final Color CYAN4;

    /** the color "aquamarine1" */
    public static final Color AQUAMARINE1;

    /** the color "aquamarine2" */
    public static final Color AQUAMARINE2;

    /** the color "aquamarine3" */
    public static final Color AQUAMARINE3;

    /** the color "aquamarine4" */
    public static final Color AQUAMARINE4;

    /** the color "green1" */
    public static final Color GREEN1;

    /** the color "green2" */
    public static final Color GREEN2;

    /** the color "green3" */
    public static final Color GREEN3;

    /** the color "green4" */
    public static final Color GREEN4;

    /** the color "chartreuse1" */
    public static final Color CHARTREUSE1;

    /** the color "chartreuse2" */
    public static final Color CHARTREUSE2;

    /** the color "chartreuse3" */
    public static final Color CHARTREUSE3;

    /** the color "chartreuse4" */
    public static final Color CHARTREUSE4;

    /** the color "khaki1" */
    public static final Color KHAKI1;

    /** the color "khaki2" */
    public static final Color KHAKI2;

    /** the color "khaki3" */
    public static final Color KHAKI3;

    /** the color "khaki4" */
    public static final Color KHAKI4;

    /** the color "yellow1" */
    public static final Color YELLOW1;

    /** the color "yellow2" */
    public static final Color YELLOW2;

    /** the color "yellow3" */
    public static final Color YELLOW3;

    /** the color "yellow4" */
    public static final Color YELLOW4;

    /** the color "gold1" */
    public static final Color GOLD1;

    /** the color "gold2" */
    public static final Color GOLD2;

    /** the color "gold3" */
    public static final Color GOLD3;

    /** the color "gold4" */
    public static final Color GOLD4;

    /** the color "goldenrod1" */
    public static final Color GOLDENROD1;

    /** the color "goldenrod2" */
    public static final Color GOLDENROD2;

    /** the color "goldenrod3" */
    public static final Color GOLDENROD3;

    /** the color "goldenrod4" */
    public static final Color GOLDENROD4;

    /** the color "sienna1" */
    public static final Color SIENNA1;

    /** the color "sienna2" */
    public static final Color SIENNA2;

    /** the color "sienna3" */
    public static final Color SIENNA3;

    /** the color "sienna4" */
    public static final Color SIENNA4;

    /** the color "burlywood1" */
    public static final Color BURLYWOOD1;

    /** the color "burlywood2" */
    public static final Color BURLYWOOD2;

    /** the color "burlywood3" */
    public static final Color BURLYWOOD3;

    /** the color "burlywood4" */
    public static final Color BURLYWOOD4;

    /** the color "wheat1" */
    public static final Color WHEAT1;

    /** the color "wheat2" */
    public static final Color WHEAT2;

    /** the color "wheat3" */
    public static final Color WHEAT3;

    /** the color "wheat4" */
    public static final Color WHEAT4;

    /** the color "tan1" */
    public static final Color TAN1;

    /** the color "tan2" */
    public static final Color TAN2;

    /** the color "tan3" */
    public static final Color TAN3;

    /** the color "tan4" */
    public static final Color TAN4;

    /** the color "chocolate1" */
    public static final Color CHOCOLATE1;

    /** the color "chocolate2" */
    public static final Color CHOCOLATE2;

    /** the color "chocolate3" */
    public static final Color CHOCOLATE3;

    /** the color "chocolate4" */
    public static final Color CHOCOLATE4;

    /** the color "firebrick1" */
    public static final Color FIREBRICK1;

    /** the color "firebrick2" */
    public static final Color FIREBRICK2;

    /** the color "firebrick3" */
    public static final Color FIREBRICK3;

    /** the color "firebrick4" */
    public static final Color FIREBRICK4;

    /** the color "brown1" */
    public static final Color BROWN1;

    /** the color "brown2" */
    public static final Color BROWN2;

    /** the color "brown3" */
    public static final Color BROWN3;

    /** the color "brown4" */
    public static final Color BROWN4;

    /** the color "salmon1" */
    public static final Color SALMON1;

    /** the color "salmon2" */
    public static final Color SALMON2;

    /** the color "salmon3" */
    public static final Color SALMON3;

    /** the color "salmon4" */
    public static final Color SALMON4;

    /** the color "orange1" */
    public static final Color ORANGE1;

    /** the color "orange2" */
    public static final Color ORANGE2;

    /** the color "orange3" */
    public static final Color ORANGE3;

    /** the color "orange4" */
    public static final Color ORANGE4;

    /** the color "coral2" */
    public static final Color CORAL2;

    /** the color "coral3" */
    public static final Color CORAL3;

    /** the color "coral4" */
    public static final Color CORAL4;

    /** the color "tomato1" */
    public static final Color TOMATO1;

    /** the color "tomato2" */
    public static final Color TOMATO2;

    /** the color "tomato3" */
    public static final Color TOMATO3;

    /** the color "tomato4" */
    public static final Color TOMATO4;

    /** the color "red1" */
    public static final Color RED1;

    /** the color "red2" */
    public static final Color RED2;

    /** the color "red3" */
    public static final Color RED3;

    /** the color "red4" */
    public static final Color RED4;

    /** the color "pink1" */
    public static final Color PINK1;

    /** the color "pink2" */
    public static final Color PINK2;

    /** the color "pink3" */
    public static final Color PINK3;

    /** the color "pink4" */
    public static final Color PINK4;

    /** the color "maroon2" */
    public static final Color MAROON2;

    /** the color "maroon3" */
    public static final Color MAROON3;

    /** the color "maroon4" */
    public static final Color MAROON4;

    /** the color "magenta1" */
    public static final Color MAGENTA1;

    /** the color "magenta2" */
    public static final Color MAGENTA2;

    /** the color "magenta3" */
    public static final Color MAGENTA3;

    /** the color "magenta4" */
    public static final Color MAGENTA4;

    /** the color "orchid1" */
    public static final Color ORCHID1;

    /** the color "orchid2" */
    public static final Color ORCHID2;

    /** the color "orchid3" */
    public static final Color ORCHID3;

    /** the color "orchid4" */
    public static final Color ORCHID4;

    /** the color "plum1" */
    public static final Color PLUM1;

    /** the color "plum2" */
    public static final Color PLUM2;

    /** the color "plum3" */
    public static final Color PLUM3;

    /** the color "plum4" */
    public static final Color PLUM4;

    /** the color "purple1" */
    public static final Color PURPLE1;

    /** the color "purple2" */
    public static final Color PURPLE2;

    /** the color "purple3" */
    public static final Color PURPLE3;

    /** the color "purple4" */
    public static final Color PURPLE4;

    /** the color "thistle1" */
    public static final Color THISTLE1;

    /** the color "thistle2" */
    public static final Color THISTLE2;

    /** the color "thistle3" */
    public static final Color THISTLE3;

    /** the color "grey0" */
    public static final Color GREY0;

    /** the color "grey1" */
    public static final Color GREY1;

    /** the color "grey2" */
    public static final Color GREY2;

    /** the color "grey3" */
    public static final Color GREY3;

    /** the color "grey4" */
    public static final Color GREY4;

    /** the color "grey5" */
    public static final Color GREY5;

    /** the color "grey6" */
    public static final Color GREY6;

    /** the color "grey7" */
    public static final Color GREY7;

    /** the color "grey8" */
    public static final Color GREY8;

    /** the color "grey9" */
    public static final Color GREY9;

    /** the color "grey10" */
    public static final Color GREY10;

    /** the color "grey11" */
    public static final Color GREY11;

    /** the color "grey12" */
    public static final Color GREY12;

    /** the color "grey13" */
    public static final Color GREY13;

    /** the color "grey14" */
    public static final Color GREY14;

    /** the color "grey15" */
    public static final Color GREY15;

    /** the color "grey16" */
    public static final Color GREY16;

    /** the color "grey17" */
    public static final Color GREY17;

    /** the color "grey18" */
    public static final Color GREY18;

    /** the color "grey19" */
    public static final Color GREY19;

    /** the color "grey20" */
    public static final Color GREY20;

    /** the color "grey21" */
    public static final Color GREY21;

    /** the color "grey22" */
    public static final Color GREY22;

    /** the color "grey23" */
    public static final Color GREY23;

    /** the color "grey24" */
    public static final Color GREY24;

    /** the color "grey25" */
    public static final Color GREY25;

    /** the color "grey26" */
    public static final Color GREY26;

    /** the color "grey27" */
    public static final Color GREY27;

    /** the color "grey28" */
    public static final Color GREY28;

    /** the color "grey29" */
    public static final Color GREY29;

    /** the color "grey30" */
    public static final Color GREY30;

    /** the color "grey31" */
    public static final Color GREY31;

    /** the color "grey32" */
    public static final Color GREY32;

    /** the color "grey33" */
    public static final Color GREY33;

    /** the color "grey34" */
    public static final Color GREY34;

    /** the color "grey35" */
    public static final Color GREY35;

    /** the color "grey36" */
    public static final Color GREY36;

    /** the color "grey37" */
    public static final Color GREY37;

    /** the color "grey38" */
    public static final Color GREY38;

    /** the color "grey39" */
    public static final Color GREY39;

    /** the color "grey40" */
    public static final Color GREY40;

    /** the color "grey41" */
    public static final Color GREY41;

    /** the color "grey42" */
    public static final Color GREY42;

    /** the color "grey43" */
    public static final Color GREY43;

    /** the color "grey44" */
    public static final Color GREY44;

    /** the color "grey45" */
    public static final Color GREY45;

    /** the color "grey46" */
    public static final Color GREY46;

    /** the color "grey47" */
    public static final Color GREY47;

    /** the color "grey48" */
    public static final Color GREY48;

    /** the color "grey49" */
    public static final Color GREY49;

    /** the color "grey50" */
    public static final Color GREY50;

    /** the color "grey51" */
    public static final Color GREY51;

    /** the color "grey52" */
    public static final Color GREY52;

    /** the color "grey53" */
    public static final Color GREY53;

    /** the color "grey54" */
    public static final Color GREY54;

    /** the color "grey55" */
    public static final Color GREY55;

    /** the color "grey56" */
    public static final Color GREY56;

    /** the color "grey57" */
    public static final Color GREY57;

    /** the color "grey58" */
    public static final Color GREY58;

    /** the color "grey59" */
    public static final Color GREY59;

    /** the color "grey60" */
    public static final Color GREY60;

    /** the color "grey61" */
    public static final Color GREY61;

    /** the color "grey62" */
    public static final Color GREY62;

    /** the color "grey63" */
    public static final Color GREY63;

    /** the color "grey64" */
    public static final Color GREY64;

    /** the color "grey65" */
    public static final Color GREY65;

    /** the color "grey66" */
    public static final Color GREY66;

    /** the color "grey67" */
    public static final Color GREY67;

    /** the color "grey68" */
    public static final Color GREY68;

    /** the color "grey69" */
    public static final Color GREY69;

    /** the color "grey70" */
    public static final Color GREY70;

    /** the color "grey71" */
    public static final Color GREY71;

    /** the color "grey72" */
    public static final Color GREY72;

    /** the color "grey73" */
    public static final Color GREY73;

    /** the color "grey74" */
    public static final Color GREY74;

    /** the color "grey75" */
    public static final Color GREY75;

    /** the color "grey76" */
    public static final Color GREY76;

    /** the color "grey77" */
    public static final Color GREY77;

    /** the color "grey78" */
    public static final Color GREY78;

    /** the color "grey79" */
    public static final Color GREY79;

    /** the color "grey80" */
    public static final Color GREY80;

    /** the color "grey81" */
    public static final Color GREY81;

    /** the color "grey82" */
    public static final Color GREY82;

    /** the color "grey83" */
    public static final Color GREY83;

    /** the color "grey84" */
    public static final Color GREY84;

    /** the color "grey85" */
    public static final Color GREY85;

    /** the color "grey86" */
    public static final Color GREY86;

    /** the color "grey87" */
    public static final Color GREY87;

    /** the color "grey88" */
    public static final Color GREY88;

    /** the color "grey89" */
    public static final Color GREY89;

    /** the color "grey90" */
    public static final Color GREY90;

    /** the color "grey91" */
    public static final Color GREY91;

    /** the color "grey92" */
    public static final Color GREY92;

    /** the color "grey93" */
    public static final Color GREY93;

    /** the color "grey94" */
    public static final Color GREY94;

    /** the color "grey95" */
    public static final Color GREY95;

    /** the color "grey96" */
    public static final Color GREY96;

    /** the color "grey97" */
    public static final Color GREY97;

    /** the color "grey98" */
    public static final Color GREY98;

    /** the color "grey99" */
    public static final Color GREY99;

    /** the color "grey100" */
    public static final Color GREY100;

    static {
        Display device = Display.getCurrent();
        SNOW = new Color(device, 255, 250, 250);
        GHOST_WHITE = new Color(device, 248, 248, 255);
        WHITE_SMOKE = new Color(device, 245, 245, 245);
        GAINSBORO = new Color(device, 220, 220, 220);
        FLORAL_WHITE = new Color(device, 255, 250, 240);
        OLD_LACE = new Color(device, 253, 245, 230);
        LINEN = new Color(device, 250, 240, 230);
        ANTIQUE_WHITE = new Color(device, 250, 235, 215);
        PAPAYA_WHIP = new Color(device, 255, 239, 213);
        BLANCHED_ALMOND = new Color(device, 255, 235, 205);
        BISQUE = new Color(device, 255, 228, 196);
        PEACH_PUFF = new Color(device, 255, 218, 185);
        NAVAJO_WHITE = new Color(device, 255, 222, 173);
        MOCCASIN = new Color(device, 255, 228, 181);
        CORNSILK = new Color(device, 255, 248, 220);
        IVORY = new Color(device, 255, 255, 240);
        LEMON_CHIFFON = new Color(device, 255, 250, 205);
        SEASHELL = new Color(device, 255, 245, 238);
        HONEYDEW = new Color(device, 240, 255, 240);
        MINT_CREAM = new Color(device, 245, 255, 250);
        AZURE = new Color(device, 240, 255, 255);
        ALICE_BLUE = new Color(device, 240, 248, 255);
        LAVENDER = new Color(device, 230, 230, 250);
        LAVENDER_BLUSH = new Color(device, 255, 240, 245);
        MISTY_ROSE = new Color(device, 255, 228, 225);
        WHITE = new Color(device, 255, 255, 255);
        BLACK = new Color(device, 0, 0, 0);
        DARK_SLATE_GREY = new Color(device, 47, 79, 79);
        DIM_GREY = new Color(device, 105, 105, 105);
        SLATE_GREY = new Color(device, 112, 128, 144);
        LIGHT_SLATE_GREY = new Color(device, 119, 136, 153);
        LIGHT_GREY = new Color(device, 211, 211, 211);
        MIDNIGHT_BLUE = new Color(device, 25, 25, 112);
        NAVY = new Color(device, 0, 0, 128);
        NAVY_BLUE = new Color(device, 0, 0, 128);
        CORNFLOWER_BLUE = new Color(device, 100, 149, 237);
        DARK_SLATE_BLUE = new Color(device, 72, 61, 139);
        SLATE_BLUE = new Color(device, 106, 90, 205);
        MEDIUM_SLATE_BLUE = new Color(device, 123, 104, 238);
        LIGHT_SLATE_BLUE = new Color(device, 132, 112, 255);
        MEDIUM_BLUE = new Color(device, 0, 0, 205);
        ROYAL_BLUE = new Color(device, 65, 105, 225);
        BLUE = new Color(device, 0, 0, 255);
        DODGER_BLUE = new Color(device, 30, 144, 255);
        DEEP_SKY_BLUE = new Color(device, 0, 191, 255);
        SKY_BLUE = new Color(device, 135, 206, 235);
        LIGHT_SKY_BLUE = new Color(device, 135, 206, 250);
        LIGHT_STEEL_BLUE = new Color(device, 176, 196, 222);
        LIGHT_BLUE = new Color(device, 173, 216, 230);
        POWDER_BLUE = new Color(device, 176, 224, 230);
        DARK_TURQUOISE = new Color(device, 0, 206, 209);
        MEDIUM_TURQUOISE = new Color(device, 72, 209, 204);
        TURQUOISE = new Color(device, 64, 224, 208);
        CYAN = new Color(device, 0, 255, 255);
        LIGHT_CYAN = new Color(device, 224, 255, 255);
        CADET_BLUE = new Color(device, 95, 158, 160);
        MEDIUM_AQUAMARINE = new Color(device, 102, 205, 170);
        AQUAMARINE = new Color(device, 127, 255, 212);
        DARK_GREEN = new Color(device, 0, 100, 0);
        DARK_OLIVE_GREEN = new Color(device, 85, 107, 47);
        DARK_SEA_GREEN = new Color(device, 143, 188, 143);
        SEA_GREEN = new Color(device, 46, 139, 87);
        MEDIUM_SEA_GREEN = new Color(device, 60, 179, 113);
        LIGHT_SEA_GREEN = new Color(device, 32, 178, 170);
        PALE_GREEN = new Color(device, 152, 251, 152);
        SPRING_GREEN = new Color(device, 0, 255, 127);
        LAWN_GREEN = new Color(device, 124, 252, 0);
        GREEN = new Color(device, 0, 255, 0);
        CHARTREUSE = new Color(device, 127, 255, 0);
        MEDIUM_SPRING_GREEN = new Color(device, 0, 250, 154);
        GREEN_YELLOW = new Color(device, 173, 255, 47);
        LIME_GREEN = new Color(device, 50, 205, 50);
        YELLOW_GREEN = new Color(device, 154, 205, 50);
        FOREST_GREEN = new Color(device, 34, 139, 34);
        OLIVE_DRAB = new Color(device, 107, 142, 35);
        DARK_KHAKI = new Color(device, 189, 183, 107);
        KHAKI = new Color(device, 240, 230, 140);
        PALE_GOLDENROD = new Color(device, 238, 232, 170);
        LIGHT_YELLOW = new Color(device, 255, 255, 224);
        YELLOW = new Color(device, 255, 255, 0);
        GOLD = new Color(device, 255, 215, 0);
        LIGHT_GOLDENROD = new Color(device, 238, 221, 130);
        GOLDENROD = new Color(device, 218, 165, 32);
        DARK_GOLDENROD = new Color(device, 184, 134, 11);
        ROSY_BROWN = new Color(device, 188, 143, 143);
        SADDLE_BROWN = new Color(device, 139, 69, 19);
        SIENNA = new Color(device, 160, 82, 45);
        PERU = new Color(device, 205, 133, 63);
        BURLYWOOD = new Color(device, 222, 184, 135);
        BEIGE = new Color(device, 245, 245, 220);
        WHEAT = new Color(device, 245, 222, 179);
        SANDY_BROWN = new Color(device, 244, 164, 96);
        TAN = new Color(device, 210, 180, 140);
        CHOCOLATE = new Color(device, 210, 105, 30);
        FIREBRICK = new Color(device, 178, 34, 34);
        BROWN = new Color(device, 165, 42, 42);
        DARK_SALMON = new Color(device, 233, 150, 122);
        SALMON = new Color(device, 250, 128, 114);
        LIGHT_SALMON = new Color(device, 255, 160, 122);
        ORANGE = new Color(device, 255, 165, 0);
        DARK_ORANGE = new Color(device, 255, 140, 0);
        CORAL = new Color(device, 255, 127, 80);
        LIGHT_CORAL = new Color(device, 240, 128, 128);
        TOMATO = new Color(device, 255, 99, 71);
        ORANGE_RED = new Color(device, 255, 69, 0);
        RED = new Color(device, 255, 0, 0);
        HOT_PINK = new Color(device, 255, 105, 180);
        DEEP_PINK = new Color(device, 255, 20, 147);
        PINK = new Color(device, 255, 192, 203);
        PALE_VIOLET_RED = new Color(device, 219, 112, 147);
        MAROON = new Color(device, 176, 48, 96);
        MEDIUM_VIOLET_RED = new Color(device, 199, 21, 133);
        VIOLET_RED = new Color(device, 208, 32, 144);
        MAGENTA = new Color(device, 255, 0, 255);
        VIOLET = new Color(device, 238, 130, 238);
        PLUM = new Color(device, 221, 160, 221);
        ORCHID = new Color(device, 218, 112, 214);
        MEDIUM_ORCHID = new Color(device, 186, 85, 211);
        DARK_ORCHID = new Color(device, 153, 50, 204);
        DARK_VIOLET = new Color(device, 148, 0, 211);
        PURPLE = new Color(device, 160, 32, 240);
        MEDIUM_PURPLE = new Color(device, 147, 112, 219);
        THISTLE = new Color(device, 216, 191, 216);
        SNOW1 = new Color(device, 255, 250, 250);
        SNOW2 = new Color(device, 238, 233, 233);
        SNOW3 = new Color(device, 205, 201, 201);
        SNOW4 = new Color(device, 139, 137, 137);
        SEASHELL1 = new Color(device, 255, 245, 238);
        SEASHELL2 = new Color(device, 238, 229, 222);
        SEASHELL3 = new Color(device, 205, 197, 191);
        SEASHELL4 = new Color(device, 139, 134, 130);
        BISQUE2 = new Color(device, 238, 213, 183);
        BISQUE3 = new Color(device, 205, 183, 158);
        BISQUE4 = new Color(device, 139, 125, 107);
        CORNSILK1 = new Color(device, 255, 248, 220);
        CORNSILK2 = new Color(device, 238, 232, 205);
        CORNSILK3 = new Color(device, 205, 200, 177);
        CORNSILK4 = new Color(device, 139, 136, 120);
        IVORY1 = new Color(device, 255, 255, 240);
        IVORY2 = new Color(device, 238, 238, 224);
        IVORY3 = new Color(device, 205, 205, 193);
        IVORY4 = new Color(device, 139, 139, 131);
        HONEYDEW1 = new Color(device, 240, 255, 240);
        HONEYDEW2 = new Color(device, 224, 238, 224);
        HONEYDEW3 = new Color(device, 193, 205, 193);
        HONEYDEW4 = new Color(device, 131, 139, 131);
        AZURE2 = new Color(device, 224, 238, 238);
        AZURE3 = new Color(device, 193, 205, 205);
        AZURE4 = new Color(device, 131, 139, 139);
        BLUE1 = new Color(device, 0, 0, 255);
        BLUE2 = new Color(device, 0, 0, 238);
        BLUE3 = new Color(device, 0, 0, 205);
        BLUE4 = new Color(device, 0, 0, 139);
        TURQUOISE1 = new Color(device, 0, 245, 255);
        TURQUOISE2 = new Color(device, 0, 229, 238);
        TURQUOISE3 = new Color(device, 0, 197, 205);
        TURQUOISE4 = new Color(device, 0, 134, 139);
        CYAN1 = new Color(device, 0, 255, 255);
        CYAN2 = new Color(device, 0, 238, 238);
        CYAN3 = new Color(device, 0, 205, 205);
        CYAN4 = new Color(device, 0, 139, 139);
        AQUAMARINE1 = new Color(device, 127, 255, 212);
        AQUAMARINE2 = new Color(device, 118, 238, 198);
        AQUAMARINE3 = new Color(device, 102, 205, 170);
        AQUAMARINE4 = new Color(device, 69, 139, 116);
        GREEN1 = new Color(device, 0, 255, 0);
        GREEN2 = new Color(device, 0, 238, 0);
        GREEN3 = new Color(device, 0, 205, 0);
        GREEN4 = new Color(device, 0, 139, 0);
        CHARTREUSE1 = new Color(device, 127, 255, 0);
        CHARTREUSE2 = new Color(device, 118, 238, 0);
        CHARTREUSE3 = new Color(device, 102, 205, 0);
        CHARTREUSE4 = new Color(device, 69, 139, 0);
        KHAKI1 = new Color(device, 255, 246, 143);
        KHAKI2 = new Color(device, 238, 230, 133);
        KHAKI3 = new Color(device, 205, 198, 115);
        KHAKI4 = new Color(device, 139, 134, 78);
        YELLOW1 = new Color(device, 255, 255, 0);
        YELLOW2 = new Color(device, 238, 238, 0);
        YELLOW3 = new Color(device, 205, 205, 0);
        YELLOW4 = new Color(device, 139, 139, 0);
        GOLD1 = new Color(device, 255, 215, 0);
        GOLD2 = new Color(device, 238, 201, 0);
        GOLD3 = new Color(device, 205, 173, 0);
        GOLD4 = new Color(device, 139, 117, 0);
        GOLDENROD1 = new Color(device, 255, 193, 37);
        GOLDENROD2 = new Color(device, 238, 180, 34);
        GOLDENROD3 = new Color(device, 205, 155, 29);
        GOLDENROD4 = new Color(device, 139, 105, 20);
        SIENNA1 = new Color(device, 255, 130, 71);
        SIENNA2 = new Color(device, 238, 121, 66);
        SIENNA3 = new Color(device, 205, 104, 57);
        SIENNA4 = new Color(device, 139, 71, 38);
        BURLYWOOD1 = new Color(device, 255, 211, 155);
        BURLYWOOD2 = new Color(device, 238, 197, 145);
        BURLYWOOD3 = new Color(device, 205, 170, 125);
        BURLYWOOD4 = new Color(device, 139, 115, 85);
        WHEAT1 = new Color(device, 255, 231, 186);
        WHEAT2 = new Color(device, 238, 216, 174);
        WHEAT3 = new Color(device, 205, 186, 150);
        WHEAT4 = new Color(device, 139, 126, 102);
        TAN1 = new Color(device, 255, 165, 79);
        TAN2 = new Color(device, 238, 154, 73);
        TAN3 = new Color(device, 205, 133, 63);
        TAN4 = new Color(device, 139, 90, 43);
        CHOCOLATE1 = new Color(device, 255, 127, 36);
        CHOCOLATE2 = new Color(device, 238, 118, 33);
        CHOCOLATE3 = new Color(device, 205, 102, 29);
        CHOCOLATE4 = new Color(device, 139, 69, 19);
        FIREBRICK1 = new Color(device, 255, 48, 48);
        FIREBRICK2 = new Color(device, 238, 44, 44);
        FIREBRICK3 = new Color(device, 205, 38, 38);
        FIREBRICK4 = new Color(device, 139, 26, 26);
        BROWN1 = new Color(device, 255, 64, 64);
        BROWN2 = new Color(device, 238, 59, 59);
        BROWN3 = new Color(device, 205, 51, 51);
        BROWN4 = new Color(device, 139, 35, 35);
        SALMON1 = new Color(device, 255, 140, 105);
        SALMON2 = new Color(device, 238, 130, 98);
        SALMON3 = new Color(device, 205, 112, 84);
        SALMON4 = new Color(device, 139, 76, 57);
        ORANGE1 = new Color(device, 255, 165, 0);
        ORANGE2 = new Color(device, 238, 154, 0);
        ORANGE3 = new Color(device, 205, 133, 0);
        ORANGE4 = new Color(device, 139, 90, 0);
        CORAL2 = new Color(device, 238, 106, 80);
        CORAL3 = new Color(device, 205, 91, 69);
        CORAL4 = new Color(device, 139, 62, 47);
        TOMATO1 = new Color(device, 255, 99, 71);
        TOMATO2 = new Color(device, 238, 92, 66);
        TOMATO3 = new Color(device, 205, 79, 57);
        TOMATO4 = new Color(device, 139, 54, 38);
        RED1 = new Color(device, 255, 0, 0);
        RED2 = new Color(device, 238, 0, 0);
        RED3 = new Color(device, 205, 0, 0);
        RED4 = new Color(device, 139, 0, 0);
        PINK1 = new Color(device, 255, 181, 197);
        PINK2 = new Color(device, 238, 169, 184);
        PINK3 = new Color(device, 205, 145, 158);
        PINK4 = new Color(device, 139, 99, 108);
        MAROON2 = new Color(device, 238, 48, 167);
        MAROON3 = new Color(device, 205, 41, 144);
        MAROON4 = new Color(device, 139, 28, 98);
        MAGENTA1 = new Color(device, 255, 0, 255);
        MAGENTA2 = new Color(device, 238, 0, 238);
        MAGENTA3 = new Color(device, 205, 0, 205);
        MAGENTA4 = new Color(device, 139, 0, 139);
        ORCHID1 = new Color(device, 255, 131, 250);
        ORCHID2 = new Color(device, 238, 122, 233);
        ORCHID3 = new Color(device, 205, 105, 201);
        ORCHID4 = new Color(device, 139, 71, 137);
        PLUM1 = new Color(device, 255, 187, 255);
        PLUM2 = new Color(device, 238, 174, 238);
        PLUM3 = new Color(device, 205, 150, 205);
        PLUM4 = new Color(device, 139, 102, 139);
        PURPLE1 = new Color(device, 155, 48, 255);
        PURPLE2 = new Color(device, 145, 44, 238);
        PURPLE3 = new Color(device, 125, 38, 205);
        PURPLE4 = new Color(device, 85, 26, 139);
        THISTLE1 = new Color(device, 255, 225, 255);
        THISTLE2 = new Color(device, 238, 210, 238);
        THISTLE3 = new Color(device, 205, 181, 205);
        GREY0 = new Color(device, 0, 0, 0);
        GREY1 = new Color(device, 3, 3, 3);
        GREY2 = new Color(device, 5, 5, 5);
        GREY3 = new Color(device, 8, 8, 8);
        GREY4 = new Color(device, 10, 10, 10);
        GREY5 = new Color(device, 13, 13, 13);
        GREY6 = new Color(device, 15, 15, 15);
        GREY7 = new Color(device, 18, 18, 18);
        GREY8 = new Color(device, 20, 20, 20);
        GREY9 = new Color(device, 23, 23, 23);
        GREY10 = new Color(device, 26, 26, 26);
        GREY11 = new Color(device, 28, 28, 28);
        GREY12 = new Color(device, 31, 31, 31);
        GREY13 = new Color(device, 33, 33, 33);
        GREY14 = new Color(device, 36, 36, 36);
        GREY15 = new Color(device, 38, 38, 38);
        GREY16 = new Color(device, 41, 41, 41);
        GREY17 = new Color(device, 43, 43, 43);
        GREY18 = new Color(device, 46, 46, 46);
        GREY19 = new Color(device, 48, 48, 48);
        GREY20 = new Color(device, 51, 51, 51);
        GREY21 = new Color(device, 54, 54, 54);
        GREY22 = new Color(device, 56, 56, 56);
        GREY23 = new Color(device, 59, 59, 59);
        GREY24 = new Color(device, 61, 61, 61);
        GREY25 = new Color(device, 64, 64, 64);
        GREY26 = new Color(device, 66, 66, 66);
        GREY27 = new Color(device, 69, 69, 69);
        GREY28 = new Color(device, 71, 71, 71);
        GREY29 = new Color(device, 74, 74, 74);
        GREY30 = new Color(device, 77, 77, 77);
        GREY31 = new Color(device, 79, 79, 79);
        GREY32 = new Color(device, 82, 82, 82);
        GREY33 = new Color(device, 84, 84, 84);
        GREY34 = new Color(device, 87, 87, 87);
        GREY35 = new Color(device, 89, 89, 89);
        GREY36 = new Color(device, 92, 92, 92);
        GREY37 = new Color(device, 94, 94, 94);
        GREY38 = new Color(device, 97, 97, 97);
        GREY39 = new Color(device, 99, 99, 99);
        GREY40 = new Color(device, 102, 102, 102);
        GREY41 = new Color(device, 105, 105, 105);
        GREY42 = new Color(device, 107, 107, 107);
        GREY43 = new Color(device, 110, 110, 110);
        GREY44 = new Color(device, 112, 112, 112);
        GREY45 = new Color(device, 115, 115, 115);
        GREY46 = new Color(device, 117, 117, 117);
        GREY47 = new Color(device, 120, 120, 120);
        GREY48 = new Color(device, 122, 122, 122);
        GREY49 = new Color(device, 125, 125, 125);
        GREY50 = new Color(device, 127, 127, 127);
        GREY51 = new Color(device, 130, 130, 130);
        GREY52 = new Color(device, 133, 133, 133);
        GREY53 = new Color(device, 135, 135, 135);
        GREY54 = new Color(device, 138, 138, 138);
        GREY55 = new Color(device, 140, 140, 140);
        GREY56 = new Color(device, 143, 143, 143);
        GREY57 = new Color(device, 145, 145, 145);
        GREY58 = new Color(device, 148, 148, 148);
        GREY59 = new Color(device, 150, 150, 150);
        GREY60 = new Color(device, 153, 153, 153);
        GREY61 = new Color(device, 156, 156, 156);
        GREY62 = new Color(device, 158, 158, 158);
        GREY63 = new Color(device, 161, 161, 161);
        GREY64 = new Color(device, 163, 163, 163);
        GREY65 = new Color(device, 166, 166, 166);
        GREY66 = new Color(device, 168, 168, 168);
        GREY67 = new Color(device, 171, 171, 171);
        GREY68 = new Color(device, 173, 173, 173);
        GREY69 = new Color(device, 176, 176, 176);
        GREY70 = new Color(device, 179, 179, 179);
        GREY71 = new Color(device, 181, 181, 181);
        GREY72 = new Color(device, 184, 184, 184);
        GREY73 = new Color(device, 186, 186, 186);
        GREY74 = new Color(device, 189, 189, 189);
        GREY75 = new Color(device, 191, 191, 191);
        GREY76 = new Color(device, 194, 194, 194);
        GREY77 = new Color(device, 196, 196, 196);
        GREY78 = new Color(device, 199, 199, 199);
        GREY79 = new Color(device, 201, 201, 201);
        GREY80 = new Color(device, 204, 204, 204);
        GREY81 = new Color(device, 207, 207, 207);
        GREY82 = new Color(device, 209, 209, 209);
        GREY83 = new Color(device, 212, 212, 212);
        GREY84 = new Color(device, 214, 214, 214);
        GREY85 = new Color(device, 217, 217, 217);
        GREY86 = new Color(device, 219, 219, 219);
        GREY87 = new Color(device, 222, 222, 222);
        GREY88 = new Color(device, 224, 224, 224);
        GREY89 = new Color(device, 227, 227, 227);
        GREY90 = new Color(device, 229, 229, 229);
        GREY91 = new Color(device, 232, 232, 232);
        GREY92 = new Color(device, 235, 235, 235);
        GREY93 = new Color(device, 237, 237, 237);
        GREY94 = new Color(device, 240, 240, 240);
        GREY95 = new Color(device, 242, 242, 242);
        GREY96 = new Color(device, 245, 245, 245);
        GREY97 = new Color(device, 247, 247, 247);
        GREY98 = new Color(device, 250, 250, 250);
        GREY99 = new Color(device, 252, 252, 252);
        GREY100 = new Color(device, 255, 255, 255);
    }
}
