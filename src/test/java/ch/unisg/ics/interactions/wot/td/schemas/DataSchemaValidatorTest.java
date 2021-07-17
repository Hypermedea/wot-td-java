package ch.unisg.ics.interactions.wot.td.schemas;

import org.apache.commons.collections4.map.HashedMap;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static ch.unisg.ics.interactions.wot.td.schemas.DataSchemaValidator.validate;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DataSchemaValidatorTest {

  @Test
  public void testValidateStringSchema() {

    DataSchema stringSchema = new StringSchema.Builder().build();

    assertTrue(validate(stringSchema, "1"));

    assertFalse(validate(stringSchema, 1));
    assertFalse(validate(stringSchema, 1.5));
    assertFalse(validate(stringSchema, true));
    assertFalse(validate(stringSchema, null));

    List<String> arrayValue = new ArrayList<>();
    arrayValue.add("1");
    assertFalse(validate(stringSchema, arrayValue));

    Map<String, Object> objectValue = new HashedMap<>();
    objectValue.put("name", "Rimuru");
    assertFalse(validate(stringSchema, objectValue));
  }

  @Test
  public void testValidateNumberSchema() {

    DataSchema numberSchema = new NumberSchema.Builder().build();

    assertTrue(validate(numberSchema, 1));
    assertTrue(validate(numberSchema, (float) 1.5));
    assertTrue(validate(numberSchema, (long) 1.5));

    assertFalse(validate(numberSchema, "1"));
    assertFalse(validate(numberSchema, true));
    assertFalse(validate(numberSchema, null));

    List<String> arrayValue = new ArrayList<>();
    arrayValue.add("1");
    assertFalse(validate(numberSchema, arrayValue));

    Map<String, Object> objectValue = new HashedMap<>();
    objectValue.put("name", "Rimuru");
    assertFalse(validate(numberSchema, objectValue));
  }

  @Test
  public void testValidateIntegerSchema() {

    DataSchema integerSchema = new IntegerSchema.Builder().build();

    assertTrue(validate(integerSchema, 1));

    assertFalse(validate(integerSchema, (float) 1.5));
    assertFalse(validate(integerSchema, (long) 1.5));
    assertFalse(validate(integerSchema, "1"));
    assertFalse(validate(integerSchema, true));
    assertFalse(validate(integerSchema, null));

    List<String> arrayValue = new ArrayList<>();
    arrayValue.add("1");
    assertFalse(validate(integerSchema, arrayValue));

    Map<String, Object> objectValue = new HashedMap<>();
    objectValue.put("name", "Rimuru");
    assertFalse(validate(integerSchema, objectValue));
  }

  @Test
  public void testValidateBooleanSchema() {

    DataSchema booleanSchema = new BooleanSchema.Builder().build();

    assertTrue(validate(booleanSchema, true));

    assertFalse(validate(booleanSchema, 1));
    assertFalse(validate(booleanSchema, 1.5));
    assertFalse(validate(booleanSchema, "1"));
    assertFalse(validate(booleanSchema, null));

    List<String> arrayValue = new ArrayList<>();
    arrayValue.add("1");
    assertFalse(validate(booleanSchema, arrayValue));

    Map<String, Object> objectValue = new HashedMap<>();
    objectValue.put("name", "Rimuru");
    assertFalse(validate(booleanSchema, objectValue));
  }

  @Test
  public void testValidateArraySchemaSize() {

    ArraySchema arraySchema = new ArraySchema.Builder()
      .addMinItems(2)
      .addMaxItems(2)
      .build();

    List<String> arrayValueEqualItems = new ArrayList<>(Arrays.asList("1", "2"));
    assertTrue(validate(arraySchema, arrayValueEqualItems));

    List<String> arrayValueLessItems = new ArrayList<>(Arrays.asList("1"));
    assertFalse(validate(arraySchema, arrayValueLessItems));

    List<String> arrayValueMoreItems = new ArrayList<>(Arrays.asList("1", "2", "3"));
    assertFalse(validate(arraySchema, arrayValueMoreItems));
  }

  @Test
  public void testValidateArraySchemaOneItem() {

    ArraySchema arraySchema = new ArraySchema.Builder()
      .addItem(new StringSchema.Builder().build())
      .build();

    assertTrue(validate(arraySchema, Arrays.asList("1", "2")));

    assertFalse(validate(arraySchema, Arrays.asList(1, 2)));
    assertFalse(validate(arraySchema, Arrays.asList(1.5, 2.5)));
    assertFalse(validate(arraySchema, Arrays.asList(true, false)));
    assertFalse(validate(arraySchema, Arrays.asList("1", null)));
    assertFalse(validate(arraySchema, Arrays.asList("1", 2)));
  }

  @Test
  public void testValidateArraySchema() {

    DataSchema arraySchema = new ArraySchema.Builder().build();
    List<String> arrayValue = new ArrayList<>();
    arrayValue.add("1");
    assertTrue(validate(arraySchema, arrayValue));

    assertFalse(validate(arraySchema, 1));
    assertFalse(validate(arraySchema, 1.5));
    assertFalse(validate(arraySchema, "1"));
    assertFalse(validate(arraySchema, true));
    assertFalse(validate(arraySchema, null));

    Map<String, Object> objectValue = new HashedMap<>();
    objectValue.put("name", "Rimuru");
    assertFalse(validate(arraySchema, objectValue));
  }

  @Test
  public void testValidateNullSchema() {

    DataSchema nullSchema = new NullSchema.Builder().build();

    assertTrue(validate(nullSchema, null));

    assertFalse(validate(nullSchema, "1"));
    assertFalse(validate(nullSchema, 1));
    assertFalse(validate(nullSchema, 1.5));
    assertFalse(validate(nullSchema, true));

    List<String> arrayValue = new ArrayList<>();
    arrayValue.add("Rimuru");
    assertFalse(validate(nullSchema, arrayValue));

    Map<String, Object> objectValue = new HashedMap<>();
    objectValue.put("name", "Rimuru");
    assertFalse(validate(nullSchema, objectValue));
  }
}
