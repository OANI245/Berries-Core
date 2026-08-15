package berries.servermod.tcm.block;

import org.jetbrains.annotations.NotNull;
import org.mtr.mapping.registry.CreativeModeTabHolder;
import org.mtr.mod.CreativeModeTabs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BlockId {
    @NotNull String value();
    String modid() default "tcm";
    ItemGroupFor itemGroup() default ItemGroupFor.CORE;

    enum ItemGroupFor {
        CORE(CreativeModeTabs.CORE),
        RAILWAY_FACILITIES(CreativeModeTabs.RAILWAY_FACILITIES),
        STATION_BUILDING_BLOCKS(CreativeModeTabs.STATION_BUILDING_BLOCKS),
        ESCALATORS_LIFTS(CreativeModeTabs.ESCALATORS_LIFTS);

        private final CreativeModeTabHolder holder;

        private ItemGroupFor(CreativeModeTabHolder holder) {
            this.holder = holder;
        }

        public CreativeModeTabHolder getTab() {
            return holder;
        }
    }
}
