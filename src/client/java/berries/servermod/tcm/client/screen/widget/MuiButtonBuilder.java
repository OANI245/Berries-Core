package berries.servermod.tcm.client.screen.widget;

import berries.servermod.tcm.util.TCMComponent;
import icyllis.arc3d.engine.VertexInputLayout;
import icyllis.modernui.core.Context;
import icyllis.modernui.resources.ResourceId;
import icyllis.modernui.util.AttributeSet;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;

public class MuiButtonBuilder {
    private Component text = TCMComponent.text("");
    private Tuple<Integer, Integer> size = new Tuple<>(0, 0);
    private View.OnClickListener onPress = (view) -> {};
    private Button button;

    private MuiButtonBuilder(Button data) {
        this.button = data;
    }

    public static MuiButtonBuilder builder(Context context) {
        return new MuiButtonBuilder(new Button(context));
    }

    public MuiButtonBuilder setText(Component text) {
        this.text = text;
        return this;
    }

    public MuiButtonBuilder setSize(int width, int height) {
        this.size = new Tuple<>(width, height);
        return this;
    }

    public MuiButtonBuilder setSizeDP(int width, int height) {
        this.size = new Tuple<>(button.dp(width), button.dp(height));
        return this;
    }

    public MuiButtonBuilder setStyle(ResourceId style) {
        this.button = new Button(this.button.getContext(), null, style);
        return this;
    }

    public MuiButtonBuilder setStyle(ResourceId style, AttributeSet as) {
        this.button = new Button(this.button.getContext(), as, style);
        return this;
    }

    public MuiButtonBuilder onPress(View.OnClickListener event) {
        this.onPress = event;
        return this;
    }

    public Button build() {
        button.setText(text.getString());
        button.setWidth(size.getA());
        button.setHeight(size.getB());
        button.setOnClickListener(onPress);
        return button;
    }
}
