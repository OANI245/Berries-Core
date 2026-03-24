package berries.servermod.tcm.client.screen.fragments;

import berries.servermod.tcm.UFEInfo;
import berries.servermod.tcm.client.Config;
import berries.servermod.tcm.client.screen.TCMColorSchemes;
import icyllis.modernui.R;
import icyllis.modernui.core.Context;
import icyllis.modernui.fragment.FragmentContainerView;
import icyllis.modernui.fragment.FragmentTransaction;
import icyllis.modernui.graphics.Image;
import icyllis.modernui.graphics.drawable.ColorDrawable;
import icyllis.modernui.graphics.drawable.ImageDrawable;
import icyllis.modernui.graphics.drawable.RippleDrawable;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.mc.MuiModApi;
import icyllis.modernui.mc.MuiScreen;
import icyllis.modernui.mc.ui.ThemeControl;
import icyllis.modernui.resources.TypedValue;
import icyllis.modernui.util.ColorStateList;
import icyllis.modernui.util.DataSet;
import icyllis.modernui.util.DisplayMetrics;
import icyllis.modernui.util.StateSet;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.LayoutInflater;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.FrameLayout;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.RadioButton;
import icyllis.modernui.widget.RadioGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static icyllis.modernui.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static icyllis.modernui.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class MainFragment extends AbstractFragment {
    ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void init(Context context) {
        getParentFragmentManager().beginTransaction()
                .setPrimaryNavigationFragment(this)
                .commit();
    }

    @Override
    public void onCreate(DataSet savedInstanceState) {
        super.onCreate(savedInstanceState);
        var ft = getChildFragmentManager().beginTransaction();
        ft.replace(0x9001, HomeFragment.class, null, "home");
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .setReorderingAllowed(true)
                .commit();

        executor.submit(() -> {
            Minecraft mc = Minecraft.getInstance();
            int windowWidth = mc.getWindow().getScreenWidth();
            int windowHeight = mc.getWindow().getScreenHeight();

            while (
                    mc.screen instanceof MuiScreen &&
                            ((MuiScreen) mc.screen).getFragment().equals(MainFragment.this) &&
                            mc.getWindow().getScreenWidth() == windowWidth && mc.getWindow().getScreenHeight() == windowHeight
            ) {
                mc = Minecraft.getInstance();

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            if (mc.screen instanceof MuiScreen && ((MuiScreen) mc.screen).getFragment().equals(MainFragment.this)) {
                mc.execute(() -> {
                    MuiModApi.openScreen(new MainFragment());
                });
            }
            executor.shutdown();
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, DataSet savedInstanceState) {
        var base = new LinearLayout(getContext());
        base.setOrientation(LinearLayout.HORIZONTAL);
        base.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        base.setDividerDrawable(ThemeControl.makeDivider(base, true));

        //var value = new TypedValue();
        var theme = requireContext().getTheme();

        navigation:
        {
            var buttonGroup = new RadioGroup(getContext());
            //theme.resolveAttribute(R.ns, R.attr.colorSurfaceContainer, value, true);
            buttonGroup.setBackground(new ColorDrawable(TCMColorSchemes.getScheme("surface_container", Config.INSTANCE.guiDarkMode)));
            buttonGroup.setOrientation(LinearLayout.VERTICAL);
            buttonGroup.setGravity(Gravity.CENTER_HORIZONTAL);

            var icons = new Image[]{
                    Image.create(UFEInfo.MOD_ID, "gui/icon/navigation_item_home_rounded.png"),
                    Image.create(UFEInfo.MOD_ID, "gui/icon/navigation_item_player_rounded.png"),
                    Image.create(UFEInfo.MOD_ID, "gui/icon/navigation_item_about_rounded.png"),
                    Image.create(UFEInfo.MOD_ID, "gui/icon/navigation_item_settings_rounded.png")
            };

            // icons are designed for xxhigh
            for (Image icon : icons) {
                if (icon != null) {
                    icon.setDensity(DisplayMetrics.DENSITY_DEFAULT * 3);
                }
            }
            int colorOnSecondaryContainer;
            ColorStateList itemTextColor;
            {
                int[] colors = new int[2];
                //theme.resolveAttribute(R.ns, R.attr.colorSecondary, value, true);
                colors[0] = TCMColorSchemes.getScheme("secondary", Config.INSTANCE.guiDarkMode); // selected
                //theme.resolveAttribute(R.ns, R.attr.colorOnSurfaceVariant, value, true);
                colors[1] = TCMColorSchemes.getScheme("on_surface_variant", Config.INSTANCE.guiDarkMode); // other
                itemTextColor = new ColorStateList(
                        new int[][]{
                                new int[]{R.attr.state_checked},
                                StateSet.WILD_CARD
                        },
                        colors
                );
            }
            ColorStateList itemIconTint;
            {
                int[] colors = new int[2];
                //theme.resolveAttribute(R.ns, R.attr.colorOnSecondaryContainer, value, true);
                colors[0] = colorOnSecondaryContainer = TCMColorSchemes.getScheme("on_secondary_container", Config.INSTANCE.guiDarkMode); // selected
                //theme.resolveAttribute(R.ns, R.attr.colorOnSurfaceVariant, value, true);
                colors[1] = TCMColorSchemes.getScheme("on_surface_variant", Config.INSTANCE.guiDarkMode); // other
                itemIconTint = new ColorStateList(
                        new int[][]{
                                new int[]{R.attr.state_checked},
                                StateSet.WILD_CARD
                        },
                        colors
                );
            }
            ColorStateList itemRippleColor = new ColorStateList(
                    new int[][]{
                            new int[]{R.attr.state_pressed},
                            new int[]{R.attr.state_focused},
                            new int[]{R.attr.state_hovered},
                            StateSet.WILD_CARD
                    },
                    new int[]{
                            ColorStateList.modulateColor(colorOnSecondaryContainer, 0.1f),
                            ColorStateList.modulateColor(colorOnSecondaryContainer, 0.1f),
                            ColorStateList.modulateColor(colorOnSecondaryContainer, 0.08f),
                            ColorStateList.modulateColor(colorOnSecondaryContainer, 0.08f)
                    }
            );
            ColorStateList activeIndicatorColor;
            {
                int[] colors = new int[2];
                //theme.resolveAttribute(R.ns, R.attr.colorSecondaryContainer, value, true);
                colors[0] = TCMColorSchemes.getScheme("secondary_container", Config.INSTANCE.guiDarkMode); // selected
                activeIndicatorColor = new ColorStateList(
                        new int[][]{
                                new int[]{R.attr.state_checked},
                                StateSet.WILD_CARD
                        },
                        colors
                );
            }

            createNavButton(buttonGroup, 9001, "gui.tcm.main.nav.1",
                    itemTextColor, icons[0],
                    itemIconTint, itemRippleColor, activeIndicatorColor, false, true);
            createNavButton(buttonGroup, 9002, "gui.tcm.main.nav.2",
                    itemTextColor, icons[1],
                    itemIconTint, itemRippleColor, activeIndicatorColor, false, false);
            createNavButton(buttonGroup, 9003, "gui.tcm.main.nav.3",
                    itemTextColor, icons[2],
                    itemIconTint, itemRippleColor, activeIndicatorColor, false, false);
            createNavButton(buttonGroup, 9004, "gui.tcm.main.nav.4",
                    itemTextColor, icons[3],
                    itemIconTint, itemRippleColor, activeIndicatorColor, true, true);

            var params = new LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT);
            params.gravity = Gravity.CENTER;
            base.addView(buttonGroup, params);
        }

        frame:
        {
            var tabContainer = new FragmentContainerView(getContext());
            tabContainer.setId(0x9001);
            var params = new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
            base.addView(tabContainer, params);
        }


        var params = new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        base.setLayoutParams(params);
        return base;
    }

    private RadioButton createNavButton(RadioGroup group, int id, String text,
                                        ColorStateList itemTextColor,
                                        Image icons,
                                        ColorStateList itemIconTint,
                                        ColorStateList itemRippleColor,
                                        ColorStateList activeIndicatorColor,
                                        boolean bottom, boolean isFirst) {
        var button = new RadioButton(getContext(), null, null, null);
        button.setFocusable(true);
        button.setClickable(true);
        button.setId(id);
        button.setText(ThemeControl.stripFormattingCodes(I18n.get(text)));
        button.setTextSize(12);
        button.setTextColor(itemTextColor);
        button.setGravity(Gravity.CENTER_HORIZONTAL);
        final int dp8 = button.dp(8);
        final int dpt = button.dp(16);
        button.setPadding(0, isFirst && !bottom ? dpt : dp8, 0, isFirst && bottom ? dpt : dp8);

        {
            ShapeDrawable indicator = new ShapeDrawable();
            indicator.setSize(button.dp(56), button.dp(32));
            indicator.setColor(activeIndicatorColor);
            indicator.setCornerRadius(1000);
            RippleDrawable ripple = new RippleDrawable(itemRippleColor,
                    indicator, null);
            if (icons != null) {
                ImageDrawable icon = new ImageDrawable(requireContext().getResources(), icons);
                icon.setSrcRect(0, 0, 72, 72);
                icon.setTintList(itemIconTint);
                icon.setGravity(Gravity.CENTER);
                ripple.addLayer(icon);
            }
            button.setCompoundDrawablesWithIntrinsicBounds(null,
                    ripple, null, null);
            button.setCompoundDrawablePadding(button.dp(4));
        }

        var params = new LinearLayout.LayoutParams(button.dp(88), WRAP_CONTENT);
        if (bottom) {
            params.setMarginsRelative(0, Minecraft.getInstance().getWindow().getScreenHeight() - (button.dp(102) + button.dp(64) * group.getChildCount()), 0, 0);
        }
        button.setLayoutParams(params);

        group.addView(button);
        return button;
    }

    @Override
    public void dispose() {
        executor.shutdown();
    }
}
