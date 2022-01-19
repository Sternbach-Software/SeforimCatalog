/***************************************************************************
 *   Copyright (C) 2010-2015 by                                            *
 *      Itamar Syn-Hershko <itamar at code972 dot com>                     *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License           *
 *   version 3, as published by the Free Software Foundation.              *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU Affero General Public License for more details.                   *
 *                                                                         *
 *   You should have received a copy of the GNU Affero General Public      *
 *   License along with this program; if not, see                          *
 *   <http://www.gnu.org/licenses/>.                                       *
 **************************************************************************/
package lemmatizer.hebmorph.tests;

import com.code972.hebmorph.datastructures.DictHebMorph;
import com.code972.hebmorph.hspell.HSpellDictionaryLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

public abstract class TestBase {
    public static String DICT_PATH = "/Users/shmuel/AndroidStudioProjects/SeforimCatalog/hspell-data-files";
    private static DictHebMorph dict;

    protected static synchronized DictHebMorph getDictionary() throws IOException {
        if (dict == null) {
            dict = new HSpellDictionaryLoader().loadDictionaryFromPath(DICT_PATH);
        }
        return dict;
    }

    protected static String readFileToString(String path) throws IOException {
        try (FileInputStream stream = new FileInputStream(new File(path))) {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            /* Instead of using default, pass in a decoder. */
            return StandardCharsets.UTF_8.decode(bb).toString();
        }
    }
}
