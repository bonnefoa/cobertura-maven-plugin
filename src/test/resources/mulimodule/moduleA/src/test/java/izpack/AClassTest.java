package izpack;

import org.junit.Test;
import org.junit.internal.matchers.StringContains;

import static org.junit.Assert.assertThat;

/**
 * @author Anthonin Bonnefoy (vfkc3065)
 */
public class AClassTest
{


    @Test
    public void testAClassFromAModule()
    {
        final AClass aClass = new AClass();
        final String result = aClass.aMethodTestedByModuleA("ny");
        assertThat(result, StringContains.containsString("haskell"));
    }
}

