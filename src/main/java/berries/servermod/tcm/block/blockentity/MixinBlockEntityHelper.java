package berries.servermod.tcm.block.blockentity;

import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface MixinBlockEntityHelper {
    @Nullable
    static <T extends BlockEntity, R> R invokeGetMethodInBlockEntity(@NotNull T target, @NotNull String methodName, Class<R> returns) {
        try {
            Method method = target.getClass().getMethod(methodName);
            var result = method.invoke(target);
            return (R) result;
        } catch (Throwable e) {
            return null;
        }
    }

    static <T extends BlockEntity> void invokeSetMethodInBlockEntity(@NotNull T target, @NotNull String methodName, Object... values) {
        try {
            List<Class<?>> classOfValues = new ArrayList<>(values.length);
            Arrays.stream(values).forEach(value -> classOfValues.add(value.getClass()));
            Method method = target.getClass().getMethod(methodName, classOfValues.toArray(new Class[0]));
            method.invoke(target, values);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    static <T extends BlockEntity> void invokeSetMethodInBlockEntity(@NotNull T target, int ignored, @NotNull String methodName, Object[] values, List<Class<?>> classOfValues) {
        try {
            Method method = target.getClass().getMethod(methodName, classOfValues.toArray(new Class[0]));
            method.invoke(target, values);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
