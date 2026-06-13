package com.jsj.datacenter.infrastructure.common.text;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ConvertTest {

    // --- toStr ---
    @Test
    void toStr_withNull_returnsDefault() {
        assertEquals("N/A", Convert.toStr(null, "N/A"));
    }

    @Test
    void toStr_withString_returnsItself() {
        assertEquals("hello", Convert.toStr("hello"));
    }

    @Test
    void toStr_withNonString_callsToString() {
        assertEquals("42", Convert.toStr(42, "N/A"));
    }

    @Test
    void toStr_withNullNoDefault_returnsNull() {
        assertNull(Convert.toStr(null));
    }

    // --- toChar ---
    @Test
    void toChar_withCharacter_returnsItself() {
        assertEquals('A', Convert.toChar('A'));
    }

    @Test
    void toChar_withString_returnsFirstChar() {
        assertEquals('H', Convert.toChar("Hello"));
    }

    @Test
    void toChar_withEmptyString_returnsDefault() {
        assertNull(Convert.toChar(""));
    }

    // --- toByte ---
    @Test
    void toByte_withNumber_returnsByteValue() {
        assertEquals((byte) 42, Convert.toByte(42));
    }

    @Test
    void toByte_withInvalidString_returnsDefault() {
        assertNull(Convert.toByte("abc"));
    }

    @Test
    void toByte_withNull_returnsDefault() {
        assertEquals((byte) 0, Convert.toByte(null, (byte) 0));
    }

    // --- toShort ---
    @Test
    void toShort_withNumber_returnsShortValue() {
        assertEquals((short) 100, Convert.toShort(100));
    }

    @Test
    void toShort_withInvalidString_returnsDefault() {
        assertNull(Convert.toShort("abc"));
    }

    // --- toInt ---
    @Test
    void toInt_withInteger_returnsItself() {
        assertEquals(42, Convert.toInt(42));
    }

    @Test
    void toInt_withString_parsesCorrectly() {
        assertEquals(42, Convert.toInt("42"));
    }

    @Test
    void toInt_withWhitespace_trimsCorrectly() {
        assertEquals(42, Convert.toInt(" 42 "));
    }

    @Test
    void toInt_withInvalidString_returnsDefault() {
        assertNull(Convert.toInt("abc"));
    }

    @Test
    void toInt_withNull_returnsDefault() {
        assertEquals(0, Convert.toInt(null, 0));
    }

    @Test
    void toInt_withNull_returnsNull() {
        assertNull(Convert.toInt(null));
    }

    // --- toIntArray ---
    @Test
    void toIntArray_withEmptyString_returnsEmptyArray() {
        assertArrayEquals(new Integer[]{}, Convert.toIntArray(""));
    }

    @Test
    void toIntArray_withCommaSeparatedString_parsesCorrectly() {
        assertArrayEquals(new Integer[]{1, 2, 3}, Convert.toIntArray("1,2,3"));
    }

    @Test
    void toIntArray_withCustomDelimiter() {
        assertArrayEquals(new Integer[]{1, 2, 3}, Convert.toIntArray(":", "1:2:3"));
    }

    // --- toLongArray ---
    @Test
    void toLongArray_withEmptyString_returnsEmptyArray() {
        assertArrayEquals(new Long[]{}, Convert.toLongArray(""));
    }

    @Test
    void toLongArray_withCommaSeparatedString_parsesCorrectly() {
        assertArrayEquals(new Long[]{1L, 2L, 3L}, Convert.toLongArray("1,2,3"));
    }

    // --- toStrArray ---
    @Test
    void toStrArray_withEmptyString_returnsEmptyArray() {
        assertArrayEquals(new String[]{}, Convert.toStrArray(""));
    }

    @Test
    void toStrArray_withDelimiter_splitsCorrectly() {
        assertArrayEquals(new String[]{"a", "b", "c"}, Convert.toStrArray(":", "a:b:c"));
    }

    // --- toLong ---
    @Test
    void toLong_withLong_returnsItself() {
        assertEquals(100L, Convert.toLong(100L));
    }

    @Test
    void toLong_withString_parsesCorrectly() {
        assertEquals(42L, Convert.toLong("42"));
    }

    @Test
    void toLong_withScientificNotation() {
        assertEquals(100L, Convert.toLong("1e2"));
    }

    @Test
    void toLong_withInvalidString_returnsDefault() {
        assertNull(Convert.toLong("abc"));
    }

    // --- toDouble ---
    @Test
    void toDouble_withDouble_returnsItself() {
        assertEquals(3.14, Convert.toDouble(3.14));
    }

    @Test
    void toDouble_withString_parsesCorrectly() {
        assertEquals(3.14, Convert.toDouble("3.14"));
    }

    @Test
    void toDouble_withScientificNotation() {
        assertEquals(100.0, Convert.toDouble("1e2"));
    }

    @Test
    void toDouble_withInvalidString_returnsDefault() {
        assertNull(Convert.toDouble("abc"));
    }

    // --- toFloat ---
    @Test
    void toFloat_withFloat_returnsItself() {
        assertEquals(1.5f, Convert.toFloat(1.5f));
    }

    @Test
    void toFloat_withString_parsesCorrectly() {
        assertEquals(1.5f, Convert.toFloat("1.5"));
    }

    @Test
    void toFloat_withInvalidString_returnsDefault() {
        assertNull(Convert.toFloat("abc"));
    }

    // --- toBool ---
    @Test
    void toBool_withBoolean_returnsItself() {
        assertTrue(Convert.toBool(true));
    }

    @Test
    void toBool_withTrueString() {
        assertTrue(Convert.toBool("true"));
    }

    @Test
    void toBool_withYesString() {
        assertTrue(Convert.toBool("yes"));
    }

    @Test
    void toBool_withOneString() {
        assertTrue(Convert.toBool("1"));
    }

    @Test
    void toBool_withFalseString() {
        assertFalse(Convert.toBool("false"));
    }

    @Test
    void toBool_withZeroString() {
        assertFalse(Convert.toBool("0"));
    }

    @Test
    void toBool_withInvalidString_returnsDefault() {
        assertNull(Convert.toBool("maybe"));
    }

    // --- toEnum ---
    enum TestColor { RED, GREEN, BLUE }

    @Test
    void toEnum_withMatchingString_returnsEnum() {
        assertEquals(TestColor.RED, Convert.toEnum(TestColor.class, "RED"));
    }

    @Test
    void toEnum_withNonExistent_returnsDefault() {
        assertEquals(TestColor.GREEN, Convert.toEnum(TestColor.class, "YELLOW", TestColor.GREEN));
    }

    // --- toBigInteger ---
    @Test
    void toBigInteger_withBigInteger_returnsItself() {
        BigInteger val = new BigInteger("123456");
        assertEquals(val, Convert.toBigInteger(val));
    }

    @Test
    void toBigInteger_withString_parsesCorrectly() {
        assertEquals(new BigInteger("123456"), Convert.toBigInteger("123456"));
    }

    @Test
    void toBigInteger_withLong_usesValueOf() {
        assertEquals(BigInteger.valueOf(42L), Convert.toBigInteger(42L));
    }

    // --- toBigDecimal ---
    @Test
    void toBigDecimal_withBigDecimal_returnsItself() {
        BigDecimal val = new BigDecimal("123.45");
        assertEquals(val, Convert.toBigDecimal(val));
    }

    @Test
    void toBigDecimal_withString_parsesCorrectly() {
        assertEquals(new BigDecimal("123.45"), Convert.toBigDecimal("123.45"));
    }

    @Test
    void toBigDecimal_withDouble_usesValueOf() {
        assertEquals(new BigDecimal(3.14), Convert.toBigDecimal(3.14));
    }

    @Test
    void toBigDecimal_withInteger_usesNewBigDecimal() {
        assertEquals(new BigDecimal(42), Convert.toBigDecimal(42));
    }

    // --- str (byte array) ---
    @Test
    void str_withByteArray_andCharset() {
        byte[] bytes = "hello".getBytes(StandardCharsets.UTF_8);
        assertEquals("hello", Convert.str(bytes, StandardCharsets.UTF_8));
    }

    @Test
    void str_withNullBytearray_returnsNull() {
        assertNull(Convert.str((byte[]) null, StandardCharsets.UTF_8));
    }

    @Test
    void str_withByteBuffer() {
        java.nio.ByteBuffer buf = java.nio.ByteBuffer.wrap("hello".getBytes());
        assertEquals("hello", Convert.str(buf, StandardCharsets.UTF_8));
    }

    @Test
    void str_withNullBuffer_returnsNull() {
        assertNull(Convert.str((java.nio.ByteBuffer) null, StandardCharsets.UTF_8));
    }

    // --- toSBC / toDBC ---
    @Test
    void toSBC_convertsHalfWidthToFullWidth() {
        assertEquals("　", Convert.toSBC(" "));
    }

    @Test
    void toDBC_convertsFullWidthToHalfWidth() {
        assertEquals(" ", Convert.toDBC("　"));
    }

    @Test
    void toSBC_withNotConvertSet_skipsSpecifiedChars() {
        java.util.Set<Character> skip = new java.util.HashSet<>();
        skip.add(' ');
        assertEquals(" ", Convert.toSBC(" ", skip));
    }

    // --- digitUppercase ---
    @Test
    void digitUppercase_withZero() {
        String result = Convert.digitUppercase(0);
        assertNotNull(result);
    }

    @Test
    void digitUppercase_withPositiveNumber() {
        String result = Convert.digitUppercase(100);
        assertNotNull(result);
    }
}
