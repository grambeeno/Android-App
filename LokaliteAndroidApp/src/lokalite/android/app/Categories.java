package lokalite.android.app;

import java.util.ArrayList;
import java.util.Arrays;

public final class Categories {

	/**
	 * List of cats for businesses. Add a new entry here to have it 
	 * shown in eventcats.
	 */
	private static final String[] business = {
		"All",
		"Featured",
		"Happy Hour",
		"Music",
		"Retail",
		"Food & Dining",
		"Arts & Entertainment",
		"Sports & Recreation",
		"Health & Medical",
		"Beauty & Spas",
		"Campus",
		"Community",
		"Business & Tech",
		"Non-Profit",
		"Other"
	};

	/**
	 * Lists of cats of Events. Add a new entry here to have it
	 *  shown 
	 */
	private static final String[] events = {
		"All",
		"Featured",
		"Happy Hour",
		"Music",
		"Retail",
		"Food & Dining",
		"Arts & Entertainment",
		"Sports & Recreation",
		"Health & Medical",
		"Beauty & Spas",
		"Campus",
		"Community",
		"Business & Tech",
		"Non-Profit",
		"Other"
	};
	
	
	public static final ArrayList<String> businessCategories = new ArrayList<String>(Arrays.asList(business));
	public static final ArrayList<String> eventCategories = new ArrayList<String>(Arrays.asList(events));

}
