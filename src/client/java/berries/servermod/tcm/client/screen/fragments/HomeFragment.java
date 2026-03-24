package berries.servermod.tcm.client.screen.fragments;

import berries.servermod.tcm.client.Config;
import berries.servermod.tcm.client.screen.TCMColorSchemes;
import berries.servermod.tcm.client.screen.TCMMainScreen;
import berries.servermod.tcm.client.screen.widget.MuiButtonBuilder;
import berries.servermod.tcm.util.TCMComponent;
import icyllis.modernui.R;
import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.Canvas;
import icyllis.modernui.graphics.drawable.ColorDrawable;
import icyllis.modernui.graphics.drawable.Drawable;
import icyllis.modernui.material.MaterialDrawable;
import icyllis.modernui.material.drawable.RadioButtonDrawable;
import icyllis.modernui.util.AttributeSet;
import icyllis.modernui.util.DataSet;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.LayoutInflater;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.*;
import net.minecraft.client.Minecraft;

public class HomeFragment extends AbstractFragment {

    private View layout = null;

    public HomeFragment() {
        super();
    }

    @Override
    public void init(Context context) {
    }

    @Override
    public void dispose() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, DataSet savedInstanceState) {
        if (layout != null) {
            return layout;
        }

        var guiGraphics = new AbsoluteLayout(requireContext());

        Button button1 = MuiButtonBuilder.builder(requireContext()).setStyle(R.attr.buttonStyle).setSizeDP(200, 50)
                .setText(TCMComponent.text("Test"))
                .onPress((button) -> Minecraft.getInstance().tell(() -> {
                    TCMMainScreen.open(Minecraft.getInstance(), null);
                }))
                .build();

        guiGraphics.addView(button1);

        layout = guiGraphics;
        return layout;
    }
}
