package fr.isep;

import java.util.Base64;

/**
 * L'operateur binaire XOR travaille octet par octet.
 * Java utilisant UTF8 par défaut, certains caractères sont soit sur un byte soit sur 2 ou 3.
 * Cela rends obligatoire la conversion en tableau de bytes.
 *
 * Le codage/decodage étant symétrique, c'est la même méthode pour faire les deux actions.
 *
 * source:
 * https://stackoverflow.com/questions/5126616/xor-operation-with-two-strings-in-java
 */
public class XORCoder {

    public static byte[] codeDecode(final byte[] input, final byte[] secret) {
        final byte[] output = new byte[input.length];
        if (secret.length == 0) {
            throw new IllegalArgumentException("empty security key");
        }
        int spos = 0;
        for (int pos = 0; pos < input.length; ++pos) {
            output[pos] = (byte) (input[pos] ^ secret[spos]);
            ++spos;
            if (spos >= secret.length) {
                spos = 0;
            }
        }
        return output;
    }


}
