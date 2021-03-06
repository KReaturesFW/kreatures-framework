package com.github.kreatures.island.operators;

import static com.github.kreatures.island.data.IslandDesires.FILL_BATTERY;
import static com.github.kreatures.island.data.IslandDesires.FIND_SHELTER;
import static com.github.kreatures.island.data.IslandDesires.FINISH_WORK;
import static com.github.kreatures.island.data.IslandDesires.SECURE_SITE;
import static com.github.kreatures.island.enums.Weather.THUNDERSTORM;

import java.util.Set;

import com.github.kreatures.core.Desire;
import com.github.kreatures.secrecy.operators.parameter.PlanParameter;
import com.github.kreatures.island.components.Area;
import com.github.kreatures.island.components.Battery;
import com.github.kreatures.simple.operators.FilterOperator;

/**
 * 
 * @author Manuel Barbi
 *
 */
public class IslandFilterOperator extends FilterOperator {

	@Override
	protected Desire choose(Set<Desire> options, Desire pursued, PlanParameter param) {
		Area area = param.getAgent().getComponent(Area.class);
		Battery battery = param.getAgent().getComponent(Battery.class);

		if ((battery.getCharge() <= 4 || pursued == FILL_BATTERY) && options.contains(FILL_BATTERY)) {
			// if battery low or last pursued intention was 'fill battery'
			// then fill battery
			if (options.contains(SECURE_SITE)) {
				// secure site before leaving
				return SECURE_SITE;
			} else {
				return FILL_BATTERY;
			}
		} else if (area.getWeather().getWeather(0) == THUNDERSTORM) {
			// if weather is dangerous then find shelter
			if (!area.isShelter() && pursued != FIND_SHELTER && options.contains(SECURE_SITE)) {
				// secure site before leaving
				return SECURE_SITE;
			} else {
				return FIND_SHELTER;
			}
		} else if (options.contains(FINISH_WORK)) {
			// otherwise just work
			return FINISH_WORK;
		}

		return null;
	}

}
