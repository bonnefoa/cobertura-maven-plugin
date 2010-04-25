package izpack;

import org.junit.Test;
import org.junit.internal.matchers.StringContains;

import static org.junit.Assert.assertThat;

/**
 * @author Anthonin Bonnefoy (vfkc3065)
 */
public class AClassFromBModuleTest
{

    @Test
    public void testAClass()
    {
        final AClass aClass = new AClass();
        final String result = aClass.aMethodTestedByModuleB("ny");
        assertThat(result, StringContains.containsString("42"));
    }
}
