package uk.ac.horizon.babyface.model;

import java.util.HashMap;
import java.util.Map;

public class BabyData
{
	public enum Gender
	{
		girl,
		boy
	}

	public enum Ethnicity
	{
		white,
		asian,
		black,
		mixed,
		other
	}

	private final Map<String, String> images = new HashMap<>();
	private Gender gender;
	private Ethnicity ethnicity;
	private Float weight;
	private Integer age = 0;
	private Integer due = 0;

	public Map<String, String> getImages()
	{
		return images;
	}

	public Integer getAge()
	{
		return age;
	}

	public Integer getDue()
	{
		return due;
	}

	public Ethnicity getEthnicity()
	{
		return ethnicity;
	}

	public Gender getGender()
	{
		return gender;
	}

	public Float getWeight()
	{
		return weight;
	}
}
