import fr.isep.XORCoder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestXOR {

    private final String PASSWD = "xorlesheriffsheriffdelespace";

    @Test
    public void testSimpleChaine(){
        String test1 = "ce sont les paroles";
        byte[] tb = XORCoder.codeDecode(test1.getBytes(), PASSWD.getBytes());
        // chiffré en xor

        byte[] tb2 = XORCoder.codeDecode(tb, PASSWD.getBytes());
        String s = new String(tb2);
        assertEquals(s, test1 );
    }

    public void testSimpleTextePlusLongQuePassword(){
        String test1 = "ce sont les parolesce sont les parolesce sont les paroles";
        byte[] tb = XORCoder.codeDecode(test1.getBytes(), PASSWD.getBytes());
        // chiffré en xor

        byte[] tb2 = XORCoder.codeDecode(tb, PASSWD.getBytes());
        String s = new String(tb2);
        assertEquals(s, test1 );
    }
}
