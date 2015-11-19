package org.springframework.cloud.consul.config;

/**
 * There are many ways in which we can specify configuration in consul i.e.,
 *
 * <ol>
 *    <li>
 *       Nested key value style: Where value is either a constant or part of the key (nested).
 *       For e.g.,
 *       For following configuration
 *       a.b.c=something
 *       a.b.d=something else
 *       One can specify the configuration in consul with
 *       key as "../kv/config/application/a/b/c" and value as "something" and
 *       key as "../kv/config/application/a/b/d" and value as "something else"
 *    </li>
 *    <li>
 *       Entire contents of properties file as value
 *       For e.g.,
 *       For following configuration
 *       a.b.c=something
 *       a.b.d=something else
 *       One can specify the configuration in consul with
 *       key as "../kv/config/application/properties" and value as whole configuration
 *       "
 *       a.b.c=something
 *       a.b.d=something else
 *       "
 *    </li>
 *    <li>
 *       as Json or YML. You get it.
 *    </li>
 * </ol>
 *
 * This enum specifies the different Formats/styles supported for loading the configuration.
 *
 * @author srikalyan.swayampakula
 */
public enum ConsulConfigFormat {
	/**
	 * Indicates that the configuration specified in consul is of type native key values.
	 */
	KEY_VALUE,

	/**
	 * Indicates that the configuration specified in consul is of property style i.e.,
	 * value of the consul key would be a list of key=value pairs separated by new lines.
	 */
	PROPERTIES,

	/**
	 * Indicates that the configuration specified in consul is of YAML style i.e.,
	 * value of the consul key would be YAML format
	 */
	YAML;

	public static ConsulConfigFormat fromString(String value) {
		if (value == null) {
			return null;
		} else if (KEY_VALUE.name().equals(value)) {
			return ConsulConfigFormat.KEY_VALUE;
		} else if (PROPERTIES.name().equals(value)) {
			return ConsulConfigFormat.PROPERTIES;
		} else if(YAML.name().equals(value)) {
			return ConsulConfigFormat.YAML;
		} else {
			throw new IllegalArgumentException(
				value + " cannot be decoded to any ConsulConfigFormat value");
		}
	}
}
