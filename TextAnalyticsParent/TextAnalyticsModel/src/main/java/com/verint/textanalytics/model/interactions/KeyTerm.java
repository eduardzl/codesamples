package com.verint.textanalytics.model.interactions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.regex.Pattern;

/**
 * Represents an utterance's Keyterm.
 * 
 * @author imor
 *
 */
@AllArgsConstructor
@NoArgsConstructor
public class KeyTerm {

	@Getter
	@Setter
	@Accessors(chain = true)
	private String name;

	@Getter
	@Setter
	@Accessors(chain = true)
	private String value;

	@Getter
	@Setter
	private int levelNumber;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((value == null) ? 0 : (value.split(Pattern.quote("|"))[0]).hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KeyTerm other = (KeyTerm) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!(value.split(Pattern.quote("|"))[0]).equals(other.value.split(Pattern.quote("|"))[0]))
			return false;
		return true;
	}

}
