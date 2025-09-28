package com.krei.cmpackagecouriers.compat.factory_abstractions;

import net.minecraft.network.FriendlyByteBuf;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_abstractions.generic.stack.GenericStackSerializer;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;

// Shamelessly copied from Create: Mobile Packages
public class FactoryAbstractionsCompat {
    public static void writeGenericOrder(FriendlyByteBuf buffer, GenericOrder order) {
        order.write(buffer);
    }

    public static GenericOrder readGenericOrder(FriendlyByteBuf buffer) {
        return GenericOrder.read(buffer);
    }

    public static void writeGenericStack(FriendlyByteBuf buffer, GenericStack stack) {
        GenericStackSerializer.write(stack, buffer);
    }

    public static GenericStack readGenericStack(FriendlyByteBuf buffer) {
        return GenericStackSerializer.read(buffer);
    }
}
