package izpack;

import org.junit.Test;
import org.junit.internal.matchers.StringContains;

import static org.junit.Assert.assertThat;

/**
 * @author Anthonin Bonnefoy (vfkc3065)
 */
public class BClassTest
{
    @Test
    public void testModuleBClassB() throws Exception
    {
        final izpack.BClass bClass = new BClass();
        final String s = bClass.moduleBClassB("g");
        assertThat(s, StringContains.containsString("g"));
    }
}
