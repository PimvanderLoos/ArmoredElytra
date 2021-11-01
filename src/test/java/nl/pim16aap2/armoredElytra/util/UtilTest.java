package nl.pim16aap2.armoredElytra.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UtilTest
{
    @Test
    void testSnakeToCamelCase()
    {
        Assertions.assertEquals("testCase", Util.snakeToCamelCase("TeSt_Case"));
        Assertions.assertEquals("testCase", Util.snakeToCamelCase("____test_case"));
        Assertions.assertEquals("", Util.snakeToCamelCase("________"));
        Assertions.assertEquals("testCase", Util.snakeToCamelCase("TeSt__Case____"));
        Assertions.assertEquals("t", Util.snakeToCamelCase("_T_"));
        Assertions.assertEquals("testcase", Util.snakeToCamelCase("TeStCase"));
    }
}
