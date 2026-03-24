package berries.servermod.tcm.client.screen.widget;

import berries.servermod.tcm.client.screen.TCMMainScreen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.util.Tuple;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Page {
    public boolean isRenderable = false;
    public double scroll;
    public final int id;

    public Page(int id) {
        this.id = id;
    }

    public abstract List<Tuple<Integer, AbstractWidget>> getWidgets();

    public abstract List<Tuple<Integer, MultiLineLabel>> getTextLabels();

    public abstract void addWidget(AbstractWidget widget);

    public void addWidgets(AbstractWidget[] widgets) {
        for (AbstractWidget widget : widgets) {
            if (widget != null) {
                addWidget(widget);
            }
        }
    }

    public void addAllRenderable(Function<AbstractWidget, AbstractWidget> fun) {
        getWidgets().forEach((widget) -> {
            if (widget.getB() instanceof AbstractWidget) {
                fun.apply((AbstractWidget) widget.getB());
            }
        });
        this.isRenderable = true;
    }

    public void removeAllRenderable(Consumer<AbstractWidget> fun) {
        getWidgets().forEach((widget) -> {
            if (widget.getB() instanceof AbstractWidget) {
                fun.accept((AbstractWidget) widget.getB());
            }
        });
        this.isRenderable = false;
    }

    public void setScroll(double scroll) {
        this.scroll = scroll;
        for (Tuple<Integer, AbstractWidget> widget : getWidgets()) {
            int defaultY = widget.getA();
            AbstractWidget gettedWidget = widget.getB();
            if (gettedWidget instanceof AbstractWidget) {
                ((AbstractWidget) gettedWidget).setY((int) (defaultY - scroll));
            }
        }
    }

    public int getHeight() {
        List<Tuple<Integer, AbstractWidget>> widgets = getWidgets();
        for (int i = widgets.size() - 1; i >= 0; i--) {
            AbstractWidget widget = widgets.get(i).getB();
            if (widget instanceof AbstractWidget) {
                return ((AbstractWidget) widget).getY() + ((AbstractWidget) widget).getHeight() + 20;
            }
        }
        return 0;
    }
}