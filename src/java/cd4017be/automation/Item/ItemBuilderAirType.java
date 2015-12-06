package cd4017be.automation.Item;

import cd4017be.automation.Automation;
import cd4017be.lib.DefaultItem;

public class ItemBuilderAirType extends DefaultItem {

	public ItemBuilderAirType(String id, String tex) 
	{
		super(id);
		this.setTextureName(tex);
        this.setCreativeTab(Automation.tabAutomation);
	}

}
