/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreemnets.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0 
 * (the "License"); you may not use this file except in compliance with 
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package opennlp.maxent.io;

import java.io.*;
import java.util.zip.*;

/**
 * A program to convert from java binary doubles to ascii.  With the new
 * conversion utililities provided in Maxent 1.2 this probably won't be
 * necessary, but it doesn't do any harm to keep it around for now.
 *
 * @author      Jason Baldridge and Gann Bierner
 * @version     $Revision: 1.1 $, $Date: 2009/01/22 23:23:33 $
 */

public class BinToAscii {

	public static void main(String[] args) throws IOException {
		PrintWriter out =
			new PrintWriter(new OutputStreamWriter(
				new GZIPOutputStream(
					new FileOutputStream(args[1]))));
		DataInputStream in =
			new DataInputStream(new GZIPInputStream(
				new FileInputStream(args[0])));

		double d;
		try {
			while(true)
				out.println(in.readDouble());
		} catch (Exception E) {}
		out.close();
		in.close();
	}

}
