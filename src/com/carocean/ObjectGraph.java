package com.carocean;

import com.carocean.theme.LauncherCarrousel;
import com.carocean.theme.LauncherCarrouselCoordinator;
import com.carocean.theme.LauncherClassic;
import com.carocean.theme.LauncherClassicCoordinator;
import com.carocean.theme.LauncherMotion;
import com.carocean.theme.LauncherMotionCoordinator;

/**
 * All the objects. In real life might be generated, e.g. by Dagger.
 */
class ObjectGraph {
	private final LauncherClassic launcherClassic = new LauncherClassic();
	private final LauncherMotion launcherMotion = new LauncherMotion();
	private final LauncherCarrousel launcherCarrousel = new LauncherCarrousel();

	private LauncherClassicCoordinator classicCoordinator() {
		return new LauncherClassicCoordinator(launcherClassic);
	}

	private LauncherMotionCoordinator motionViewCoordinator() {
		return new LauncherMotionCoordinator(launcherMotion);
	}

	private LauncherCarrouselCoordinator carrouselViewCoordinator() {
		return new LauncherCarrouselCoordinator(launcherCarrousel);
	}

	/**
	 * 
	 * @param className
	 * @return
	 */
	@SuppressWarnings("unchecked")
	<T> T get(String className) {

		if (LauncherClassicCoordinator.class.getName().equals(className)) {
			return (T) classicCoordinator();
		}

		if (LauncherMotionCoordinator.class.getName().equals(className)) {
			return (T) motionViewCoordinator();
		}

		if (LauncherCarrouselCoordinator.class.getName().equals(className)) {
			return (T) carrouselViewCoordinator();
		}

		throw new IllegalArgumentException("Unknown class: " + className);
	}
}
