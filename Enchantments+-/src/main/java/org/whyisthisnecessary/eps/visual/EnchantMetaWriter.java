package org.whyisthisnecessary.eps.visual;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.whyisthisnecessary.eps.EPS;
import org.whyisthisnecessary.eps.Main;
import org.whyisthisnecessary.eps.api.EPSConfiguration;
import org.whyisthisnecessary.eps.api.Reloadable;
import org.whyisthisnecessary.eps.legacy.Label;
import org.whyisthisnecessary.eps.util.Dictionary;

public class EnchantMetaWriter implements Listener, Reloadable {
	
	protected static Map<Enchantment, String> enchantnames = new HashMap<Enchantment, String>();
	private static boolean ve = Main.Config.getBoolean("show-vanilla-enchants-in-lore");
	private static boolean show_enchant_lore = Main.Config.getBoolean("show-enchant-lore");
	private static boolean show_enchant_descriptions_in_lore = Main.Config.getBoolean("show-enchant-descriptions-in-lore");
	private static Map<Enchantment, ArrayList<String>> descriptionMap = new HashMap<>();
	private static List<String> allDescriptionLines = new ArrayList<String>();
	private static String prefix = Main.Config.getString("enchant-lore-color").replaceAll("&", "�");
	private static File loreExemptions = new File(Main.plugin.getDataFolder(), "lore_exemptions.yml");
	private static EPSConfiguration loreExemptionsConfig = null;
	private static List<Material> exemptions = new ArrayList<Material>();
	private static final Dictionary dictionary = EPS.getDictionary();
	
	public EnchantMetaWriter()
	{
		Main.saveDefaultFile("/lore_exemptions.yml", loreExemptions);
		loreExemptionsConfig = EPSConfiguration.loadConfiguration(loreExemptions);
		for (String s : loreExemptionsConfig.getStringList("blacklist"))
			exemptions.add(Material.matchMaterial(s));
	}
	
	private static List<String> getWrittenEnchantLore(ItemStack item)
	{
		ItemMeta meta = item.getItemMeta();
		if (meta == null)
			return (new ArrayList<String>());
		if (!show_enchant_lore)
			return meta.getLore();
		Map<Enchantment, Integer> map = meta.getEnchants();
		List<String> list = meta.getLore() == null ? new ArrayList<String>() : meta.getLore();
		Collection<Enchantment> enchants = ve ? new ArrayList<Enchantment>(Arrays.asList(Enchantment.values())) : Label.values();
		
		for (Enchantment enchant : enchants)
		{	
			for (int i=0;i<list.size();i++)
			{
				String s = list.get(i);
				if (s.split(" ").length > 1)
					if (s.contains(enchantnames.get(enchant)))
						list.remove(i);
				if (allDescriptionLines.contains(s) || s.equals(ChatColor.BLACK+"-"))
					list.remove(i);
			}
		}
		
		for (Map.Entry<Enchantment,Integer> entry : map.entrySet())  
		{
			if (enchants.contains(entry.getKey()))
			{
				String name = enchantnames.get(entry.getKey());
				String lore = name+" "+ getNumber(entry.getValue());
				String s = Main.Config.getString("custom-lore-color."+dictionary.getName(entry.getKey()));
				lore = s == null ? prefix + lore : s.replaceAll("&", "�") + lore;
			    if (show_enchant_descriptions_in_lore)
			    {
				    List<String> l = getDescription(entry.getKey());
				    for (int i=l.size()-1;i>-1;i--)
				    	if (l.get(i) != "")
				    	list.add(0, l.get(i));
			    }
			    list.add(0, lore);
			    if (show_enchant_descriptions_in_lore)
			    	list.add(0, ChatColor.BLACK+"-");
			}
		}
		return list;
	}
	
	/** Gets the modified ItemMeta of the ItemStack.
	 * Only lore is modified to match custom enchant lore.
	 * 
	 * @param item The item to modify
	 * @return The modified ItemMeta
	 */
	public static ItemMeta getWrittenMeta(ItemStack item)
	{
		if (!show_enchant_lore) 
			return item.getItemMeta();
		List<String> lore = EnchantMetaWriter.getWrittenEnchantLore(item);
		ItemMeta meta = item.getItemMeta();
		if (meta != null)
		if (lore != null)
		{
			meta.setLore(lore);
			if (ve)
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		}
		return meta;
	}
	
	/** Checks if roman numerals are enabled, then gets the String of it.
	 * 
	 * @param num The number
	 * @return The String number
	 */
	public static String getNumber(Integer num)
	{
		return Main.Config.getBoolean("use-roman-numerals") ? getRomanNumeral(num) : num.toString();
	}
	
	/** Gets the roman numeral of the specified number.
	 * Only counts to ten.
	 * 
	 * @param num The number
	 * @return The roman numberal of the number
	 */
	public static String getRomanNumeral(Integer num)
	{
		switch (num)
		{
			case 1:
				return "I";
			case 2:
				return "II";
			case 3:
				return "III";
			case 4:
				return "IV";
			case 5:
				return "V";
			case 6:
				return "VI";
			case 7:
				return "VII";
			case 8:
				return "VIII";
			case 9:
				return "IX";
			case 10:
				return "X";
			default:
				return num.toString();
		}
	}
	
	/** Checks if the String is a roman numeral
	 * Only counts to ten.
	 * 
	 * @param num The number
	 * @return If the String is a roman numeral
	 */
	public static Boolean isRomanNumeral(String num)
	{
		switch (num)
		{
			case "I":
			case "II":
			case "III":
			case "IV":
			case "V":
			case "VI":
			case "VII":
			case "VIII":
			case "IX":
			case "X":
				return true;
			default:
				return false;
		}
	}
	
	public static List<String> getWrittenEnchantLoreBook(ItemStack item)
	{
		EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
		if (meta == null)
			return (new ArrayList<String>());
		Map<Enchantment, Integer> map = meta.getStoredEnchants();
		List<String> list = meta.getLore() == null ? new ArrayList<String>() : meta.getLore();
		
		if (show_enchant_lore == false)
			return list;

		Collection<Enchantment> enchants = ve ? new ArrayList<Enchantment>(Arrays.asList(Enchantment.values())) : Label.values();
		
		for (Enchantment enchant : enchants)
		{
			for (int i=0;i<list.size();i++)
			{
				String s = list.get(i);
				if (s.split(" ").length > 1)
				if (s.startsWith(ChatColor.GRAY+enchantnames.get(enchant)))
					list.remove(i);
				if (allDescriptionLines.contains(s))
					list.remove(i);
			}
		}
		
		for (Map.Entry<Enchantment,Integer> entry : map.entrySet())  
		{
			if (enchants.contains(entry.getKey()))
			{
				String name = enchantnames.get(entry.getKey());
				String lore = ChatColor.GRAY+name+" "+ getNumber(entry.getValue());
			    list.add(0, lore);
			}
		}
		return list;
	}
	
	public static EnchantmentStorageMeta getWrittenMetaBook(ItemStack item)
	{
		if (!show_enchant_lore) 
			return (EnchantmentStorageMeta)item.getItemMeta();
		List<String> lore = EnchantMetaWriter.getWrittenEnchantLoreBook(item);
		EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
		if (meta != null)
		if (lore != null)
		meta.setLore(lore);
		if (ve)
			meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
		return meta;
	}
	
	@SuppressWarnings("unchecked")
	protected static List<String> getDescription(Enchantment enchant)
	{
		ArrayList<String> o = descriptionMap.get(enchant);
		return o == null ? new ArrayList<String>() : (ArrayList<String>) o.clone();
	}
	
	private static void refreshItem(ItemStack item)
	{
		if (item == null) return;
		if (item.getType().equals(Material.ENCHANTED_BOOK)) return;
		if (exemptions.contains(item.getType())) return;
		ItemMeta meta = getWrittenMeta(item);
		if (meta == null) return;
		if (meta.getLore() != item.getItemMeta().getLore())
			item.setItemMeta(meta);
	}
	
	public static void init(Enchantment enchant)
	{
		String configdesc = EPSConfiguration.loadConfiguration(new File(Main.EnchantsFolder, dictionary.getName(enchant)+".yml")).getString("upgradedesc");
		final String desc = configdesc == null ? dictionary.getDefaultDescription(enchant) : configdesc;
		enchantnames.put(enchant, WordUtils.capitalizeFully(dictionary.getName(enchant).replaceAll("_", " ")));
		descriptionMap.put(enchant, new ArrayList<String>() {
			private static final long serialVersionUID = -5686650364578005499L;
			{
		        add("");
		        if (desc.length() > 120)
		            for (int i=0;i<=(desc.length() / 90);i++)
		            {
		            	String str = ChatColor.GRAY+desc.substring(45*i, 45*i+45 > desc.length() ? desc.length() : 45*i+45);
		            	add(str);
		            	allDescriptionLines.add(str);
		            }
		        else
		        {
		        	add(ChatColor.GRAY+desc);
		        	allDescriptionLines.add(ChatColor.GRAY+desc);
		        }
		        add("");
	    	}
		}
		);
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e)
	{
		refreshItem(e.getPlayer().getInventory().getItemInMainHand());
	}

	@Override
	public void reload() {
		ve = Main.Config.getBoolean("show-vanilla-enchants-in-lore");
		show_enchant_lore = Main.Config.getBoolean("show-enchant-lore");
		show_enchant_descriptions_in_lore = Main.Config.getBoolean("show-enchant-descriptions-in-lore");
		loreExemptionsConfig = EPSConfiguration.loadConfiguration(loreExemptions);
		prefix = Main.Config.getString("enchant-lore-color").replaceAll("&", "�");
		for (String s : loreExemptionsConfig.getStringList("blacklist"))
			exemptions.add(Material.matchMaterial(s));
		for (Enchantment enchant : Enchantment.values())
		{
			String configdesc = EPSConfiguration.loadConfiguration(new File(Main.EnchantsFolder, dictionary.getName(enchant)+".yml")).getString("upgradedesc");
			final String desc = configdesc == null ? dictionary.getDefaultDescription(enchant) : configdesc;
			
			descriptionMap.put(enchant, new ArrayList<String>() {
				private static final long serialVersionUID = -5686650364578005499L;
				{
			        add("");
			        if (desc.length() > 120)
			            for (int i=0;i<=(desc.length() / 90);i++)
			            {
			            	String str = ChatColor.GRAY+desc.substring(45*i, 45*i+45 > desc.length() ? desc.length() : 45*i+45);
			            	add(str);
			            	allDescriptionLines.add(str);
			            }
			        else
			        {
			        	add(ChatColor.GRAY+desc);
			        	allDescriptionLines.add(ChatColor.GRAY+desc);
			        }
			        add("");
		    	}
			}
			);
		}
	}
}
