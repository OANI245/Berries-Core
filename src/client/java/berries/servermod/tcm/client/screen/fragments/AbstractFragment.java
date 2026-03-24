package berries.servermod.tcm.client.screen.fragments;

import icyllis.modernui.core.Context;
import icyllis.modernui.fragment.Fragment;
import icyllis.modernui.util.DataSet;
import icyllis.modernui.view.LayoutInflater;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractFragment extends Fragment {
    public AbstractFragment() {
        super();
    }

    @Override
    public final void onAttach(@NotNull Context context) {
        super.onAttach(context);
        init(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        dispose();
    }

    public abstract void init(Context context);

    public abstract void dispose();
}
