package at.ridgo8.moreoverlays.util;

import at.ridgo8.moreoverlays.MoreOverlays;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Adapted from the version in JEI under the MIT license.
 * <a href="https://github.com/mezz/JustEnoughItems/blob/15bbaa4f8db1d77cf7ddd99178ffcd22dc20bd89/Core/src/main/java/mezz/jei/core/util/ReflectionUtil.java">ReflectionUtil.java</a>
 * @author mezz
 */
public final class ReflectionUtil {
	public static <T> Stream<T> findFieldsWithClass(Object object, Class<? extends T> fieldClass) {
		return getAllFields(object)
			.filter(field -> fieldClass.isAssignableFrom(field.getType()))
			.mapMulti((field, mapper) -> {
				try {
					field.setAccessible(true);
					Object fieldValue = field.get(object);
					if (fieldClass.isInstance(fieldValue)) {
						T cast = fieldClass.cast(fieldValue);
						mapper.accept(cast);
					}
				} catch (IllegalAccessException | InaccessibleObjectException | SecurityException e) {
					MoreOverlays.logger.error("Something went wrong. Failed to get field " + fieldClass, e);
				}
			});
	}

	private static Stream<Field> getAllFields(Object object) {
		Class<?> objectClass = object.getClass();
		List<Class<?>> classes = new ArrayList<>();
		while (objectClass != Object.class) {
			classes.add(objectClass);
			objectClass = objectClass.getSuperclass();
		}

		return classes.stream()
			.flatMap(c -> {
				try {
					Field[] fields = c.getDeclaredFields();
					return Arrays.stream(fields);
				} catch (SecurityException e) {
					MoreOverlays.logger.error("Something went wrong. Failed to get fields on " + c, e);
					return Stream.of();
				}
			});
	}
}
