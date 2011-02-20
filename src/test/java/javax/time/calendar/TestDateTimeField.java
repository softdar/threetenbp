/*
 * Copyright (c) 2011, Stephen Colebourne & Michael Nascimento Santos
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of JSR-310 nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package javax.time.calendar;

import static javax.time.calendar.ISODateTimeRule.DAY_OF_MONTH;
import static javax.time.calendar.ISODateTimeRule.DAY_OF_WEEK;
import static javax.time.calendar.ISODateTimeRule.HOUR_OF_AMPM;
import static javax.time.calendar.ISODateTimeRule.HOUR_OF_DAY;
import static javax.time.calendar.ISODateTimeRule.MINUTE_OF_HOUR;
import static javax.time.calendar.ISODateTimeRule.MONTH_OF_YEAR;
import static javax.time.calendar.ISODateTimeRule.WEEK_BASED_YEAR;
import static javax.time.calendar.ISODateTimeRule.YEAR;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test DateTimeField.
 */
@Test
public class TestDateTimeField {

    //-----------------------------------------------------------------------
    // basics
    //-----------------------------------------------------------------------
    public void test_interfaces() {
        assertTrue(Calendrical.class.isAssignableFrom(DateTimeField.class));
        assertTrue(CalendricalMatcher.class.isAssignableFrom(DateTimeField.class));
        assertTrue(Serializable.class.isAssignableFrom(DateTimeField.class));
    }

    @DataProvider(name="simple")
    Object[][] data_simple() {
        return new Object[][] {
            {DateTimeField.of(YEAR, 2008)},
            {DateTimeField.of(MONTH_OF_YEAR, 6)},
            {DateTimeField.of(MINUTE_OF_HOUR, -1)},
        };
    }

    @Test(dataProvider="simple")
    public void test_serialization(DateTimeField field) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(field);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        assertEquals(ois.readObject(), field);
    }

    public void test_immutable() {
        Class<DateTimeField> cls = DateTimeField.class;
        assertTrue(Modifier.isPublic(cls.getModifiers()));
        assertTrue(Modifier.isFinal(cls.getModifiers()));
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            assertTrue(Modifier.isFinal(field.getModifiers()), "Field:" + field.getName());
            if (Modifier.isStatic(field.getModifiers()) == false) {
                assertTrue(Modifier.isPrivate(field.getModifiers()), "Field:" + field.getName());
            }
        }
        Constructor<?>[] cons = cls.getDeclaredConstructors();
        for (Constructor<?> con : cons) {
            assertTrue(Modifier.isPrivate(con.getModifiers()));
        }
    }

    //-----------------------------------------------------------------------
    // factories
    //-----------------------------------------------------------------------
    public void factory_of_ruleLong() {
        DateTimeField test = DateTimeField.of(YEAR, 2008);
        assertField(test, YEAR, 2008);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void factory_of_nullRule() {
        DateTimeField.of(null, 1);
    }

    //-----------------------------------------------------------------------
    // withRule()
    //-----------------------------------------------------------------------
    public void test_withRule() {
        DateTimeField base = DateTimeField.of(YEAR, 2008);
        DateTimeField test = base.withRule(MONTH_OF_YEAR);
        assertField(test, MONTH_OF_YEAR, 2008);
        // check original immutable
        assertField(base, YEAR, 2008);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_withRule_nullRule() {
        DateTimeField base = DateTimeField.of(YEAR, 2008);
        base.withRule(null);
    }

    //-----------------------------------------------------------------------
    // withValue()
    //-----------------------------------------------------------------------
    public void test_withValue() {
        DateTimeField base = DateTimeField.of(YEAR, 2008);
        DateTimeField test = base.withValue(2007);
        assertField(test, YEAR, 2007);
        // check original immutable
        assertField(base, YEAR, 2008);
    }

    public void test_withValue_invalidValue() {
        DateTimeField base = DateTimeField.of(MONTH_OF_YEAR, 8);
        DateTimeField test = base.withValue(-1);
        assertField(test, MONTH_OF_YEAR, -1);
    }

    //-----------------------------------------------------------------------
    // isValidValue()
    //-----------------------------------------------------------------------
    public void test_isValidValue() {
        assertEquals(true, DateTimeField.of(MONTH_OF_YEAR, 6).isValidValue());
        assertEquals(false, DateTimeField.of(MONTH_OF_YEAR, 13).isValidValue());
        assertEquals(false, DateTimeField.of(MONTH_OF_YEAR, -1).isValidValue());
    }

    //-----------------------------------------------------------------------
    // getValidValue()
    //-----------------------------------------------------------------------
    public void test_getValidValue_valid() {
        assertEquals(6, DateTimeField.of(MONTH_OF_YEAR, 6).getValidValue());
    }

    @Test(expectedExceptions=IllegalCalendarFieldValueException.class)
    public void test_getValidValue_invalid() {
        DateTimeField test = DateTimeField.of(MONTH_OF_YEAR, 13);  // out of range
        try {
            test.getValidValue();
        } catch (IllegalCalendarFieldValueException ex) {
            assertEquals(ex.getRule(), MONTH_OF_YEAR);
            throw ex;
        }
    }

    //-----------------------------------------------------------------------
    // isValidIntValue()
    //-----------------------------------------------------------------------
    public void test_isValidIntValue() {
        assertEquals(true, DateTimeField.of(MONTH_OF_YEAR, 6).isValidValue());
        assertEquals(false, DateTimeField.of(MONTH_OF_YEAR, 13).isValidValue());
        assertEquals(false, DateTimeField.of(MONTH_OF_YEAR, -1).isValidValue());
    }

    //-----------------------------------------------------------------------
    // getValidValue()
    //-----------------------------------------------------------------------
    public void test_getValidIntValue_valid() {
        assertEquals(6, DateTimeField.of(MONTH_OF_YEAR, 6).getValidIntValue());
    }

    @Test(expectedExceptions=IllegalCalendarFieldValueException.class)
    public void test_getValidIntValue_invalid() {
        DateTimeField test = DateTimeField.of(MONTH_OF_YEAR, 13);  // out of range
        try {
            test.getValidIntValue();
        } catch (IllegalCalendarFieldValueException ex) {
            assertEquals(ex.getRule(), MONTH_OF_YEAR);
            throw ex;
        }
    }

    //-----------------------------------------------------------------------
    // getFractionalValue()
    //-----------------------------------------------------------------------
    public void test_get() {
        assertEquals(DateTimeField.of(HOUR_OF_DAY, 18).get(HOUR_OF_DAY), HOUR_OF_DAY.field(18));
        assertEquals(DateTimeField.of(HOUR_OF_DAY, 18).get(HOUR_OF_AMPM), HOUR_OF_AMPM.field(6));
        assertEquals(DateTimeField.of(HOUR_OF_DAY, 18).get(MONTH_OF_YEAR), null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_get_null() {
        DateTimeField.of(MONTH_OF_YEAR, 6).get(null);
    }

    //-----------------------------------------------------------------------
    // matchesCalendrical()
    //-----------------------------------------------------------------------
    public void test_matchesCalendrical_ymd_date() {
        LocalDate date = LocalDate.of(2008, 6, 30);
        assertEquals(DateTimeField.of(YEAR, 2008).matchesCalendrical(date), true);
        assertEquals(DateTimeField.of(YEAR, 2006).matchesCalendrical(date), false);
        assertEquals(DateTimeField.of(MONTH_OF_YEAR, 6).matchesCalendrical(date), true);
        assertEquals(DateTimeField.of(MONTH_OF_YEAR, 7).matchesCalendrical(date), false);
        assertEquals(DateTimeField.of(MONTH_OF_YEAR, -1).matchesCalendrical(date), false);
        assertEquals(DateTimeField.of(DAY_OF_MONTH, 30).matchesCalendrical(date), true);
        assertEquals(DateTimeField.of(DAY_OF_MONTH, 12).matchesCalendrical(date), false);
        assertEquals(DateTimeField.of(DAY_OF_WEEK, 1).matchesCalendrical(date), true);
        assertEquals(DateTimeField.of(DAY_OF_WEEK, 2).matchesCalendrical(date), false);
        assertEquals(DateTimeField.of(HOUR_OF_DAY, 2).matchesCalendrical(date), false);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_matchesCalendrical_null() {
        DateTimeField.of(DAY_OF_WEEK, 2).matchesCalendrical(null);
    }

    //-----------------------------------------------------------------------
    // comparteTo()
    //-----------------------------------------------------------------------
    public void test_compareTo() {
        DateTimeField a = DateTimeField.of(DAY_OF_MONTH, 7);
        DateTimeField b = DateTimeField.of(MONTH_OF_YEAR, 6);
        DateTimeField c = DateTimeField.of(MONTH_OF_YEAR, 8);
        
        assertEquals(a.compareTo(a) == 0, true);
        assertEquals(a.compareTo(b) < 0, true);
        assertEquals(a.compareTo(c) < 0, true);
        
        assertEquals(b.compareTo(a) > 0, true);
        assertEquals(b.compareTo(b) == 0, true);
        assertEquals(b.compareTo(c) < 0, true);
        
        assertEquals(c.compareTo(a) > 0, true);
        assertEquals(c.compareTo(b) > 0, true);
        assertEquals(c.compareTo(c) == 0, true);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_compareTo_null() {
        DateTimeField.of(MONTH_OF_YEAR, 6).compareTo(null);
    }

    //-----------------------------------------------------------------------
    // equals() / hashCode()
    //-----------------------------------------------------------------------
    public void test_equals1() {
        DateTimeField a = DateTimeField.of(YEAR, 2008);
        DateTimeField b = DateTimeField.of(YEAR, 2008);
        assertEquals(a.equals(b), true);
        assertEquals(b.equals(a), true);
        assertEquals(a.hashCode() == b.hashCode(), true);
        assertEquals(a.equals(a), true);
        assertEquals(b.equals(b), true);
    }

    public void test_equals2() {
        DateTimeField a = DateTimeField.of(YEAR, 2008);
        DateTimeField b = DateTimeField.of(YEAR, 2007);
        assertEquals(a.equals(b), false);
        assertEquals(b.equals(a), false);
        assertEquals(a.equals(a), true);
        assertEquals(b.equals(b), true);
    }

    public void test_equals3() {
        DateTimeField a = DateTimeField.of(YEAR, 2008);
        DateTimeField b = DateTimeField.of(WEEK_BASED_YEAR, 2008);
        assertEquals(a.equals(b), false);
        assertEquals(b.equals(a), false);
        assertEquals(a.equals(a), true);
        assertEquals(b.equals(b), true);
    }

    public void test_equals_otherType() {
        DateTimeField a = DateTimeField.of(YEAR, 2008);
        assertEquals(a.equals("Rubbish"), false);
    }

    public void test_equals_null() {
        DateTimeField a = DateTimeField.of(YEAR, 2008);
        assertEquals(a.equals(null), false);
    }

    //-----------------------------------------------------------------------
    private void assertField(
            DateTimeField field,
            DateTimeRule rule, long value) {
        assertEquals(field.getRule(), rule);
        assertEquals(field.getValue(), value);
        assertEquals(field.equals(DateTimeField.of(rule, value)), true);
        assertEquals(field.toString(), rule.getName() + " " + value);
        assertEquals(field.toDateTimeFields(), DateTimeFields.of(rule, value));
    }

}
