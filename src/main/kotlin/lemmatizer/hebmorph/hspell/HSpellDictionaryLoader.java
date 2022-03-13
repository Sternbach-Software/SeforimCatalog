package lemmatizer.hebmorph.hspell;


import lemmatizer.hebmorph.DictionaryLoader;
import lemmatizer.hebmorph.datastructures.DictHebMorph;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.HashSet;

/**
 * DictionaryLoader implementation for loading hspell data files
 */
public class HSpellDictionaryLoader implements DictionaryLoader {
    @Override
    public String dictionaryLoaderName() {
        return "hspell";
    }

    @Override
    @Deprecated
    public String[] dictionaryPossiblePaths() {
        return getPossiblePaths();
    }

    @Override
    public String[] getPossiblePaths(final String ... basePaths) {
        final HashSet<String> paths = new HashSet<>();
        if (basePaths != null) {
            for (final String basePath : basePaths) {
                paths.add(Paths.get(basePath, "hspell-data-files").toAbsolutePath().toString());
            }
        }
        paths.add("/var/lib/hspell-data-files/");
        return paths.toArray(new String[paths.size()]);
    }

    @Override
    public DictHebMorph loadDictionary(final InputStream stream) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public DictHebMorph loadDictionaryFromPath(String path) throws IOException {
//        if (path.charAt(path.length() - 1) != File.separatorChar) {
//            path += File.separatorChar;
//        }

        final File file = new File(path);
        if (file.isDirectory()) {
            return new HSpellLoader(file, true).loadDictionaryFromHSpellData(new FileInputStream(new File(path, HSpellLoader.PREFIX_H)));
        } else {
            throw new IOException("Expected a folder. Cannot load dictionary from HSpell files: " + path);
        }
    }

    @Override
    public DictHebMorph loadDictionaryFromDefaultPath() throws IOException {
        HSpellLoader loader = new HSpellLoader(new File(HSpellLoader.getHspellPath()), true);
        return loader.loadDictionaryFromHSpellData(new FileInputStream(new File(HSpellLoader.getHspellPath(), HSpellLoader.PREFIX_NOH)));
    }
}
