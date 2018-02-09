package me.skymc.totalpoints.support;

import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import me.clip.placeholderapi.external.EZPlaceholderHook;
import me.skymc.taboolib.other.NumberUtils;
import me.skymc.totalpoints.Main;

/**
 * @author sky
 * @since 2018年2月9日 下午2:31:22
 */
public class PlaceholderTotal extends EZPlaceholderHook {

	/**
	 * @param plugin
	 * @param identifier
	 */
	public PlaceholderTotal(Plugin plugin, String identifier) {
		super(plugin, identifier);
	}

	@Override
	public String onPlaceholderRequest(Player player, String args) {
		if (args.equals("total_me")) {
			return Main.getTotalPointsAPI().getDataTotal().containsKey(player.getName()) ? String.valueOf(Main.getTotalPointsAPI().getDataTotal().get(player.getName())) : "0";
		}
		if (args.equals("today_me")) {
			return Main.getTotalPointsAPI().getDataToday().containsKey(player.getName()) ? String.valueOf(Main.getTotalPointsAPI().getDataToday().get(player.getName())) : "0";
		}
		if (args.startsWith("total_top_name_")) {
			int num = NumberUtils.getInteger(args.split("_")[3]) - 1;
			// 如果数据不存在
			if (num > Main.getTotalPointsAPI().getDataTotal().size()) {
				return "-";
			}
			// 遍历数据
			int i = 0;
			for (Entry<String, Integer> entry : Main.getTotalPointsAPI().getDataTotal().entrySet()) {
				if (i == num) {
					return entry.getKey();
				}
				i++;
			}
			return "-";
		}
		if (args.startsWith("total_top_points_")) {
			int num = NumberUtils.getInteger(args.split("_")[3]) - 1;
			// 如果数据不存在
			if (num > Main.getTotalPointsAPI().getDataTotal().size()) {
				return "-";
			}
			// 遍历数据
			int i = 0;
			for (Entry<String, Integer> entry : Main.getTotalPointsAPI().getDataTotal().entrySet()) {
				if (i == num) {
					return entry.getValue() + "";
				}
				i++;
			}
			return "-";
		}
		if (args.startsWith("today_top_name_")) {
			int num = NumberUtils.getInteger(args.split("_")[3]) - 1;
			// 如果数据不存在
			if (num > Main.getTotalPointsAPI().getDataToday().size()) {
				return "-";
			}
			// 遍历数据
			int i = 0;
			for (Entry<String, Integer> entry : Main.getTotalPointsAPI().getDataToday().entrySet()) {
				if (i == num) {
					return entry.getKey();
				}
				i++;
			}
			return "-";
		}
		if (args.startsWith("today_top_points_")) {
			int num = NumberUtils.getInteger(args.split("_")[3]) - 1;
			// 如果数据不存在
			if (num > Main.getTotalPointsAPI().getDataToday().size()) {
				return "-";
			}
			// 遍历数据
			int i = 0;
			for (Entry<String, Integer> entry : Main.getTotalPointsAPI().getDataToday().entrySet()) {
				if (i == num) {
					return entry.getValue() + "";
				}
				i++;
			}
			return "-";
		}
		return null;
	}
}
