package berries.servermod.tcm.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Mixin(ErrorHandler.class)
public class MixinErrorHandler {
    /*@WrapOperation(
            method = "generateAcceptableResponse(Lberries/implementations/org/eclipse/jetty/server/Request;Lberries/implementations/jakarta/servlet/http/HttpServletRequest;Lberries/implementations/jakarta/servlet/http/HttpServletResponse;ILjava/lang/String;Ljava/lang/String;)V",
            at = @At(value = "FIELD", target = "Ljava/nio/charset/StandardCharsets;ISO_8859_1:Ljava/nio/charset/Charset;"),
            remap = false
    )
    public Charset injected01(Operation<Charset> original) {
        return StandardCharsets.UTF_8;
    }*/
}
