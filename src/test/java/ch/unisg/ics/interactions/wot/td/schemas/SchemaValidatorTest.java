package ch.unisg.ics.interactions.wot.td.schemas;

import org.apache.commons.collections4.map.HashedMap;
import org.junit.Test;

import java.util.*;

import static ch.unisg.ics.interactions.wot.td.schemas.SchemaValidator.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SchemaValidatorTest {

  private static ObjectSchema getObjectSchemaWithRequiredProperties() {
    return new ObjectSchema.Builder()
      .addProperty("requiredName", new StringSchema.Builder()
        .addSemanticType("http://example.org#Required").build())
      .addProperty("optionalName", new StringSchema.Builder()
        .addSemanticType("http://example.org#Optional").build())
      .addRequiredProperties("requiredName")
      .build();
  }

  private static ObjectSchema getObjectSchemaWithNestedObject() {
    IntegerSchema heightSchema = new IntegerSchema.Builder()
      .addSemanticType("http://example.org#Height")
      .build();

    IntegerSchema ageSchema = new IntegerSchema.Builder()
      .addSemanticType("http://example.org#Age")
      .build();

    return new ObjectSchema.Builder()
      .addProperty("slimeForm", new ObjectSchema.Builder()
        .addSemanticType("http://example.org#MainForm")
        .addProperty("height", heightSchema)
        .addProperty("age", ageSchema)
        .build())
      .addProperty("humanForm", new ObjectSchema.Builder()
        .addSemanticType("http://example.org#SecondaryForm")
        .addProperty("height", heightSchema)
        .addProperty("age", ageSchema)
        .build())
      .build();
  }

  private static ObjectSchema getObjectSchemaWithNestedArray() {
    return new ObjectSchema.Builder()
      .addProperty("forms", new ArraySchema.Builder()
        .addSemanticType("http://example.org#FormArray")
        .addItem(new StringSchema.Builder()
          .addSemanticType("http://example.org#Form")
          .build())
        .addMinItems(2)
        .build())
      .addProperty("ages", new ArraySchema.Builder()
        .addSemanticType("http://example.org#AgeArray")
        .addItem(new IntegerSchema.Builder()
          .addSemanticType("http://example.org#Age").build())
        .addItem(new IntegerSchema.Builder()
          .addSemanticType("http://example.org#Form")
          .addSemanticType("http://example.org#Age").build())
        .build())
      .build();
  }

  @Test
  public void testValidateStringSchema() {
    DataSchema stringSchema = new StringSchema.Builder().build();

    assertTrue(validate(stringSchema, "0"));
    assertTrue(validate((StringSchema) stringSchema, "0"));

    assertFalse(validate(stringSchema, 0));
    assertFalse(validate(stringSchema, 0.5));
    assertFalse(validate(stringSchema, true));

    assertFalse(validate(stringSchema, null));
    assertFalse(validate((StringSchema) stringSchema, null));

    List<String> arrayValue = new ArrayList<>();
    arrayValue.add("0");
    assertFalse(validate(stringSchema, arrayValue));

    Map<String, Object> objectValue = new HashedMap<>();
    objectValue.put("firstName", "Rimuru");
    assertFalse(validate(stringSchema, objectValue));
  }

  @Test
  public void testValidateStringSchemaEnum() {
    Set<String> enumeration = new HashSet<>();
    Collections.addAll(enumeration, "0", "1");
    DataSchema stringSchema = new StringSchema.Builder()
      .addEnum(enumeration)
      .build();

    assertTrue(validate(stringSchema, "0"));
    assertTrue(validate((StringSchema) stringSchema, "0"));

    assertFalse(validate(stringSchema, "2"));
    assertFalse(validate((StringSchema) stringSchema, "2"));
  }

  @Test
  public void testValidateNumberSchema() {
    DataSchema numberSchema = new NumberSchema.Builder().build();

    assertTrue(validate(numberSchema, 0));
    assertTrue(validate(numberSchema, (float) 0.5));
    assertTrue(validate(numberSchema, (long) 0.5));
    assertTrue(validate((NumberSchema) numberSchema, 0));
    assertTrue(validate((NumberSchema) numberSchema, (float) 0.5));
    assertTrue(validate((NumberSchema) numberSchema, (long) 0.5));

    assertFalse(validate(numberSchema, "0"));
    assertFalse(validate(numberSchema, true));
    assertFalse(validate(numberSchema, null));

    List<String> arrayValue = new ArrayList<>();
    arrayValue.add("1");
    assertFalse(validate(numberSchema, arrayValue));

    Map<String, Object> objectValue = new HashedMap<>();
    objectValue.put("firstName", "Rimuru");
    assertFalse(validate(numberSchema, objectValue));
  }

  @Test
  public void testValidateIntegerSchema() {
    DataSchema integerSchema = new IntegerSchema.Builder().build();

    assertTrue(validate(integerSchema, 0));
    assertTrue(validate((IntegerSchema) integerSchema, 1));

    assertFalse(validate(integerSchema, (float) 0.5));
    assertFalse(validate(integerSchema, (long) 0.5));
    assertFalse(validate(integerSchema, "0"));
    assertFalse(validate(integerSchema, true));
    assertFalse(validate(integerSchema, null));

    List<String> arrayValue = new ArrayList<>();
    arrayValue.add("0");
    assertFalse(validate(integerSchema, arrayValue));

    Map<String, Object> objectValue = new HashedMap<>();
    objectValue.put("firstName", "Rimuru");
    assertFalse(validate(integerSchema, objectValue));
  }

  @Test
  public void testValidateBooleanSchema() {
    DataSchema booleanSchema = new BooleanSchema.Builder().build();

    assertTrue(validate(booleanSchema, true));
    assertTrue(validate((BooleanSchema) booleanSchema, true));

    assertFalse(validate(booleanSchema, 0));
    assertFalse(validate(booleanSchema, 0.5));
    assertFalse(validate(booleanSchema, "0"));
    assertFalse(validate(booleanSchema, null));

    List<String> arrayValue = new ArrayList<>();
    arrayValue.add("0");
    assertFalse(validate(booleanSchema, arrayValue));

    Map<String, Object> objectValue = new HashedMap<>();
    objectValue.put("firstName", "Rimuru");
    assertFalse(validate(booleanSchema, objectValue));
  }

  @Test
  public void testValidateArraySchema() {
    DataSchema arraySchema = new ArraySchema.Builder().build();
    List<Object> arrayValue = new ArrayList<>();
    arrayValue.add("1");
    assertTrue(validate(arraySchema, arrayValue));
    assertTrue(validate((ArraySchema) arraySchema, arrayValue));

    assertFalse(validate(arraySchema, 0));
    assertFalse(validate(arraySchema, 0.5));
    assertFalse(validate(arraySchema, "0"));
    assertFalse(validate(arraySchema, true));

    assertFalse(validate(arraySchema, null));
    assertFalse(validate((ArraySchema) arraySchema, null));

    Map<String, Object> objectValue = new HashedMap<>();
    objectValue.put("firstName", "Rimuru");
    assertFalse(validate(arraySchema, objectValue));
  }

  @Test
  public void testValidateArraySchemaSize() {
    ArraySchema arraySchema = new ArraySchema.Builder()
      .addMinItems(2)
      .addMaxItems(2)
      .build();

    List<String> arrayValueEqualItems = new ArrayList<>(Arrays.asList("0", "1"));
    assertTrue(validate(arraySchema, arrayValueEqualItems));

    List<String> arrayValueLessItems = new ArrayList<>(Arrays.asList("0"));
    assertFalse(validate(arraySchema, arrayValueLessItems));

    List<String> arrayValueMoreItems = new ArrayList<>(Arrays.asList("0", "1", "2"));
    assertFalse(validate(arraySchema, arrayValueMoreItems));
  }

  @Test
  public void testValidateArraySchemaOneItem() {
    ArraySchema arraySchema = new ArraySchema.Builder()
      .addItem(new StringSchema.Builder().build())
      .build();

    assertTrue(validate(arraySchema, Arrays.asList("0", "1")));

    assertFalse(validate(arraySchema, Arrays.asList(0, 1)));
    assertFalse(validate(arraySchema, Arrays.asList(0.5, 1.5)));
    assertFalse(validate(arraySchema, Arrays.asList(true, false)));
    assertFalse(validate(arraySchema, Arrays.asList("0", null)));
    assertFalse(validate(arraySchema, Arrays.asList("0", 1)));
  }

  @Test
  public void testValidateArraySchemaPrimitiveItems() {
    ArraySchema arraySchema = new ArraySchema.Builder()
      .addItem(new StringSchema.Builder().build())
      .addItem(new NumberSchema.Builder().build())
      .addItem(new IntegerSchema.Builder().build())
      .addItem(new BooleanSchema.Builder().build())
      .addItem(new NullSchema.Builder().build())
      .build();

    assertTrue(validate(arraySchema, Arrays.asList("0", 0, 0, false, null)));

    assertFalse(validate(arraySchema, Arrays.asList("0", 0, 0, false)));
    assertFalse(validate(arraySchema, Arrays.asList("0", 0, 0, false, null, null)));

    assertFalse(validate(arraySchema, Arrays.asList("0", "0", "0", "false", "null")));
  }

  @Test
  public void testValidateArraySchemaNestedArray() {
    ArraySchema arraySchema = new ArraySchema.Builder()
      .addItem(new ArraySchema.Builder()
        .addItem(new StringSchema.Builder().build())
        .build())
      .addItem(new ArraySchema.Builder()
        .addItem(new NumberSchema.Builder().build())
        .build())
      .build();

    List<Object> stringValues = Arrays.asList("0", "1");
    List<Object> numberValues = Arrays.asList(1, 1);

    List<Object> values = Arrays.asList(stringValues, numberValues);
    assertTrue(validate(arraySchema, values));

    List<Object> invertedValues = Arrays.asList(numberValues, stringValues);
    assertFalse(validate(arraySchema, invertedValues));

    assertFalse(validate(arraySchema, stringValues));
    assertFalse(validate(arraySchema, numberValues));
  }

  @Test
  public void testValidateArraySchemaNestedObject() {
    ArraySchema arraySchema = new ArraySchema.Builder()
      .addItem(new ObjectSchema.Builder()
        .addProperty("height", new IntegerSchema.Builder().build())
        .addProperty("age", new IntegerSchema.Builder().build())
        .build())
      .build();

    HashedMap<String, Integer> nestedObjectValue1 = new HashedMap<>();
    HashedMap<String, Integer> nestedObjectValue2 = new HashedMap<>();

    nestedObjectValue1.put("height", 20);
    nestedObjectValue1.put("age", 2);
    nestedObjectValue2.put("height", 120);
    nestedObjectValue2.put("age", 39);

    List<Object> objectValues = Arrays.asList(nestedObjectValue1, nestedObjectValue2);

    assertTrue(validate(arraySchema, objectValues));
  }

  @Test
  public void testValidateObjectSchema() {
    DataSchema objectSchema = new ObjectSchema.Builder().build();

    Map<String, Object> objectValue = new HashedMap<>();
    objectValue.put("firstName", "Rimuru");
    objectValue.put("lastName", "Tempest");
    assertTrue(validate(objectSchema, objectValue));
    assertTrue(validate((ObjectSchema) objectSchema, objectValue));
    assertTrue(validateByPropertyNames((ObjectSchema) objectSchema, objectValue));

    Map<Object, Object> objectValueInvalidName = new HashedMap<>();
    objectValueInvalidName.put("firstName", "Rimuru");
    objectValueInvalidName.put(1, "Tempest");
    assertFalse(validate(objectSchema, objectValueInvalidName));

    assertFalse(validate(objectSchema, "1"));
    assertFalse(validate(objectSchema, 1));
    assertFalse(validate(objectSchema, 1.5));
    assertFalse(validate(objectSchema, true));
    assertFalse(validate(objectSchema, null));

    List<String> arrayValue = new ArrayList<>();
    arrayValue.add("1");
    assertFalse(validate(objectSchema, arrayValue));
  }

  @Test
  public void testValidateObjectSchemaPrimitiveProperties() {
    ObjectSchema objectSchema = new ObjectSchema.Builder()
      .addProperty("stringName", new StringSchema.Builder().build())
      .addProperty("numberName", new NumberSchema.Builder().build())
      .addProperty("integerName", new IntegerSchema.Builder().build())
      .addProperty("booleanName", new BooleanSchema.Builder().build())
      .addProperty("nullName", new NullSchema.Builder().build())
      .build();

    HashedMap<Object, Object> objectValue = new HashedMap<>();
    objectValue.put("stringName", "Rimuru");
    objectValue.put("numberName", 0);
    objectValue.put("integerName", 0);
    objectValue.put("booleanName", false);
    objectValue.put("nullName", null);

    assertTrue(validate(objectSchema, objectValue));

    objectValue.put("stringName", 0);
    assertFalse(validate(objectSchema, objectValue));
  }

  @Test
  public void testValidateObjectSchemaPropertiesSize() {
    ObjectSchema objectSchemaNoProperties = new ObjectSchema.Builder().build();
    ObjectSchema objectSchemaTwoProperties = new ObjectSchema.Builder()
      .addProperty("firstName", new StringSchema.Builder().build())
      .addProperty("lastName", new StringSchema.Builder().build())
      .build();

    HashedMap<String, Object> objectValue = new HashedMap<>();

    objectValue.put("firstName", "Rimuru");
    assertTrue(validate(objectSchemaNoProperties, objectValue));
    assertTrue(validate(objectSchemaTwoProperties, objectValue));
    assertTrue(validateByPropertyNames(objectSchemaNoProperties, objectValue));
    assertTrue(validateByPropertyNames(objectSchemaTwoProperties, objectValue));

    objectValue.put("lastName", "Tempest");
    assertTrue(validate(objectSchemaNoProperties, objectValue));
    assertTrue(validate(objectSchemaTwoProperties, objectValue));
    assertTrue(validateByPropertyNames(objectSchemaNoProperties, objectValue));
    assertTrue(validateByPropertyNames(objectSchemaTwoProperties, objectValue));

    objectValue.put("species", "Demon Slime");
    assertTrue(validate(objectSchemaNoProperties, objectValue));
    assertFalse(validate(objectSchemaTwoProperties, objectValue));
    assertTrue(validateByPropertyNames(objectSchemaNoProperties, objectValue));
    assertFalse(validateByPropertySemanticTypes(objectSchemaTwoProperties, objectValue));
  }

  @Test
  public void testValidateObjectSchemaRequiredProperties() {
    ObjectSchema objectSchema = getObjectSchemaWithRequiredProperties();

    HashedMap<String, Object> objectValue = new HashedMap<>();

    objectValue.put("optionalName", "optionalValue");
    assertFalse(validate(objectSchema, objectValue));
    assertFalse(validateByPropertyNames(objectSchema, objectValue));

    objectValue.put("requiredName", "requiredValue");
    assertTrue(validate(objectSchema, objectValue));
    assertTrue(validateByPropertyNames(objectSchema, objectValue));
  }

  @Test
  public void testValidateObjectSchemaNestedObject() {
    ObjectSchema objectSchema = getObjectSchemaWithNestedObject();

    HashedMap<String, Object> objectValue = new HashedMap<>();
    HashedMap<String, Integer> nestedObjectValue1 = new HashedMap<>();
    HashedMap<String, Integer> nestedObjectValue2 = new HashedMap<>();

    nestedObjectValue1.put("height", 20);
    nestedObjectValue1.put("age", 2);
    nestedObjectValue2.put("height", 120);
    nestedObjectValue2.put("age", 39);
    objectValue.put("slimeForm", nestedObjectValue1);
    objectValue.put("humanForm", nestedObjectValue2);

    assertTrue(validate(objectSchema, objectValue));
  }

  @Test
  public void testValidateObjectSchemaNestedArray() {
    ObjectSchema objectSchema = getObjectSchemaWithNestedArray();

    HashedMap<String, Object> objectValue = new HashedMap<>();
    objectValue.put("forms", Arrays.asList("human"));
    objectValue.put("ages", Arrays.asList(2, 39));

    assertFalse(validate(objectSchema, objectValue));

    objectValue.put("forms", Arrays.asList("human", "slime"));
    assertTrue(validate(objectSchema, objectValue));
  }

  @Test
  public void testValidateNullSchema() {
    DataSchema nullSchema = new NullSchema.Builder().build();

    assertTrue(validate(nullSchema, null));

    assertFalse(validate(nullSchema, "0"));
    assertFalse(validate(nullSchema, 0));
    assertFalse(validate(nullSchema, 0.5));
    assertFalse(validate(nullSchema, true));

    List<String> arrayValue = new ArrayList<>();
    arrayValue.add("Rimuru");
    assertFalse(validate(nullSchema, arrayValue));

    Map<String, Object> objectValue = new HashedMap<>();
    objectValue.put("firstName", "Rimuru");
    assertFalse(validate(nullSchema, objectValue));
  }

  @Test
  public void testValidateObjectSchemaNull() {
    ObjectSchema objectSchema = getObjectSchemaWithRequiredProperties();
    assertFalse(validate(objectSchema, null));
    assertFalse(validateByPropertyNames( objectSchema, null));
    assertFalse(validateByPropertyNames(objectSchema, null));

    HashedMap<String, Object> objectValueByNames = new HashedMap<>();
    objectValueByNames.put("requiredName", null);
    assertFalse(validate(objectSchema, objectValueByNames));

    HashedMap<String, Object> objectValueBySemTypes = new HashedMap<>();
    objectValueBySemTypes.put("http://example.org#Required", null);
    assertFalse(validate(objectSchema, objectValueBySemTypes));
  }

  @Test
  public void testValidateObjectSchemaBySemTypesSize() {
    ObjectSchema objectSchemaNoProperties = new ObjectSchema.Builder().build();
    ObjectSchema objectSchemaTwoProperties = new ObjectSchema.Builder()
      .addProperty("firstName", new StringSchema.Builder()
        .addSemanticType("http://example.org#FirstName").build())
      .addProperty("lastName", new StringSchema.Builder()
        .addSemanticType("http://example.org#LastName").build())
      .build();

    HashedMap<String, Object> objectValue = new HashedMap<>();
    objectValue.put("http://example.org#FirstName", "Rimuru");
    assertTrue(validate(objectSchemaNoProperties, objectValue));
    assertTrue(validate(objectSchemaTwoProperties, objectValue));
    assertTrue(validateByPropertySemanticTypes(objectSchemaNoProperties, objectValue));
    assertTrue(validateByPropertySemanticTypes(objectSchemaTwoProperties, objectValue));

    objectValue.put("http://example.org#LastName", "Tempest");
    assertTrue(validate(objectSchemaNoProperties, objectValue));
    assertTrue(validate(objectSchemaTwoProperties, objectValue));
    assertTrue(validateByPropertySemanticTypes(objectSchemaNoProperties, objectValue));
    assertTrue(validateByPropertySemanticTypes(objectSchemaTwoProperties, objectValue));

    objectValue.put("http://example.org#Species", "Demon Slime");
    assertTrue(validate(objectSchemaNoProperties, objectValue));
    assertFalse(validate(objectSchemaTwoProperties, objectValue));
    assertTrue(validateByPropertySemanticTypes(objectSchemaNoProperties, objectValue));
    assertFalse(validateByPropertySemanticTypes(objectSchemaTwoProperties, objectValue));

    assertFalse(validate(null, objectValue));
  }

  @Test
  public void testValidateObjectSchemaBySemTypesRequiredProperties() {
    ObjectSchema objectSchema = getObjectSchemaWithRequiredProperties();

    HashedMap<String, Object> objectValue = new HashedMap<>();

    objectValue.put("http://example.org#Optional", "optionalValue");
    assertFalse(validate(objectSchema, objectValue));
    assertFalse(validateByPropertySemanticTypes(objectSchema, objectValue));

    objectValue.put("http://example.org#Required", "requiredValue");
    assertTrue(validate(objectSchema, objectValue));
    assertTrue(validateByPropertySemanticTypes(objectSchema, objectValue));
  }

  @Test
  public void testValidateObjectSchemaBySemTypesNestedObject() {
    ObjectSchema objectSchema = getObjectSchemaWithNestedObject();

    HashedMap<String, Object> objectValue = new HashedMap<>();
    HashedMap<String, Integer> nestedObjectValue1 = new HashedMap<>();
    HashedMap<String, Integer> nestedObjectValue2 = new HashedMap<>();

    nestedObjectValue1.put("http://example.org#Height", 20);
    nestedObjectValue1.put("http://example.org#Age", 2);
    nestedObjectValue2.put("http://example.org#Height", 120);
    nestedObjectValue2.put("http://example.org#Age", 39);
    objectValue.put("http://example.org#MainForm", nestedObjectValue1);
    objectValue.put("http://example.org#SecondaryForm", nestedObjectValue2);

    assertTrue(validate(objectSchema, objectValue));

    nestedObjectValue1.put("http://example.org#Height", null);
    assertFalse(validate(objectSchema, objectValue));
  }

  @Test
  public void testValidateObjectSchemaBySemTypesNestedArray() {
    ObjectSchema objectSchema = getObjectSchemaWithNestedArray();

    HashedMap<String, Object> objectValue = new HashedMap<>();
    objectValue.put("http://example.org#FormArray", Arrays.asList("human"));
    objectValue.put("http://example.org#AgeArray", Arrays.asList(2, 39));

    assertFalse(validate(objectSchema, objectValue));

    objectValue.put("http://example.org#FormArray", Arrays.asList("human", "slime"));
    assertTrue(validate(objectSchema, objectValue));
  }

  @Test
  public void testValidateObjectSchemaByNamesAndSemTypes() {
    ObjectSchema simpleObjectSchema = getObjectSchemaWithRequiredProperties();

    HashedMap<String, Object> semTypesObjectValue = new HashedMap<>();
    semTypesObjectValue.put("http://example.org#Required", "requiredValue");
    semTypesObjectValue.put("http://example.org#Optional", "optionalValue");
    assertTrue(validate(simpleObjectSchema, semTypesObjectValue));

    HashedMap<String, Object> namesObjectValue = new HashedMap<>();
    namesObjectValue.put("requiredName", "requiredValue");
    namesObjectValue.put("optionalName", "optionalValue");
    assertTrue(validate(simpleObjectSchema, namesObjectValue));

    HashedMap<String, Object> mixedObjectValue1 = new HashedMap<>();
    mixedObjectValue1.put("http://example.org#Required", "requiredValue");
    mixedObjectValue1.put("optionalName", "optionalValue");
    assertFalse(validate(simpleObjectSchema, mixedObjectValue1));

    HashedMap<String, Object> mixedObjectValue2 = new HashedMap<>();
    mixedObjectValue2.put("http://example.org#Optional", "optionalValue");
    mixedObjectValue2.put("requiredName", "requiredValue");
    assertFalse(validate(simpleObjectSchema, mixedObjectValue2));
  }

  @Test
  public void testValidateObjectSchemaByNamesAndSemTypesNested() {
    ObjectSchema nestedObjectSchema = getObjectSchemaWithNestedObject();
    HashedMap<String, Object> objectValue = new HashedMap<>();
    HashedMap<String, Integer> nestedObjectValue1 = new HashedMap<>();
    HashedMap<String, Integer> nestedObjectValue2 = new HashedMap<>();
    HashedMap<String, Integer> nestedObjectValue3 = new HashedMap<>();

    nestedObjectValue1.put("http://example.org#Height", 20);
    nestedObjectValue1.put("http://example.org#Age", 2);
    nestedObjectValue2.put("http://example.org#Height", 120);
    nestedObjectValue2.put("http://example.org#Age", 39);
    objectValue.put("slimeForm", nestedObjectValue1);
    objectValue.put("humanForm", nestedObjectValue2);

    assertTrue(validate(nestedObjectSchema, objectValue));

    nestedObjectValue3.put("height", 120);
    nestedObjectValue3.put("age", 39);
    objectValue.put("humanForm", nestedObjectValue3);

    assertTrue(validate(nestedObjectSchema, objectValue));

  }

  @Test
  public void testValidateObjectSchemaDuplicateSemTypes() {
    ObjectSchema objectSchema = new ObjectSchema.Builder()
      .addProperty("a", new StringSchema.Builder()
        .addSemanticType("http://example.org#AType")
        .addSemanticType("http://example.org#CommonType")
        .build())
      .addProperty("b", new StringSchema.Builder()
        .addSemanticType("http://example.org#BType")
        .addSemanticType("http://example.org#CommonType")
        .build())
      .build();

    HashedMap<String, Object> objectValue = new HashedMap<>();
    objectValue.put("http://example.org#AType", "validValue");
    assertTrue(validate(objectSchema, objectValue));

    objectValue.put("http://example.org#CommonType", "inValidValue");
    assertFalse(validate(objectSchema, objectValue));
  }
}
