package berries.servermod.tcm.client.render;

import berries.servermod.tcm.client.util.model.DynamicModelHolder;
import org.mtr.mapping.holder.Direction;
import org.mtr.mapping.holder.World;
import org.mtr.mapping.mapper.OptimizedRenderer;
import org.mtr.mod.client.CustomResourceLoader;
import org.mtr.mod.render.MainRenderer;
import org.mtr.mod.render.QueuedRenderLayer;
import org.mtr.mod.render.StoredMatrixTransformations;
import org.mtr.mod.resource.OptimizedModelWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MTRModdedRenderManager {
    private final List<DrawEvent> events;
    
    public MTRModdedRenderManager() {
        events = new ArrayList<>();
    }
    
    public void put(DrawEvent event) {
        this.events.add(event);
    }

    public void render(World world, StoredMatrixTransformations storedMatrixTransformations, Direction facing, int light) {
        synchronized (events) {
            for (DrawEvent event : events) {
                event.run(world, storedMatrixTransformations.copy(), facing, light);
            }
        }
    }

    public void clear() {
        events.clear();
    }

    public static class MatricesDrawEvent implements DrawEvent {
        protected OptimizedModelWrapper model;
        protected StoredMatrixTransformations storedMatrixTransformations;

        public MatricesDrawEvent() {}

        public MatricesDrawEvent matrices(Stack<StoredMatrixTransformations> matrices) {
            if(matrices != null) {
                this.storedMatrixTransformations = new StoredMatrixTransformations();
                matrices.forEach((v) -> this.storedMatrixTransformations.add(v.copy()));
            } else {
                this.storedMatrixTransformations = null;
            }
            return this;
        }

        public MatricesDrawEvent model(OptimizedModelWrapper model) {
            this.model = model;
            return this;
        }

        protected MatricesDrawEvent setMatrices(StoredMatrixTransformations m) {
            this.storedMatrixTransformations = m;
            return this;
        }

        @Override
        public void run(World world, StoredMatrixTransformations storedMatrixTransformations, Direction facing, int light) {
                if (this.storedMatrixTransformations != null) {
                    storedMatrixTransformations.add(graphicsHolder -> graphicsHolder.rotateXDegrees(180));
                    storedMatrixTransformations.add(this.storedMatrixTransformations);
                    storedMatrixTransformations.add(graphicsHolder -> graphicsHolder.rotateXDegrees(-180));
                }
                if (OptimizedRenderer.hasOptimizedRendering() && this.model != null) {
                    MainRenderer.scheduleRender(QueuedRenderLayer.TEXT, (graphicsHolder, offset) -> {
                        storedMatrixTransformations.transform(graphicsHolder, offset);
                        CustomResourceLoader.OPTIMIZED_RENDERER_WRAPPER.queue(this.model, graphicsHolder, light);
                        graphicsHolder.pop();
                    });
                }
        }
    }

    /*public static class DynamicModelHolderDrawEvent implements DrawEvent {
        private DynamicModelHolder dynamicModelHolder;
        protected StoredMatrixTransformations storedMatrixTransformations;
        protected DrawEvent parent;

        public DynamicModelHolderDrawEvent() {}

        public DynamicModelHolderDrawEvent(DrawEvent parent) {
            this.parent = parent;
        }

        public DynamicModelHolderDrawEvent matrices(Stack<StoredMatrixTransformations> matrices) {
            if(matrices != null) {
                this.storedMatrixTransformations = new StoredMatrixTransformations();
                matrices.forEach(this.storedMatrixTransformations::add);
            } else {
                this.storedMatrixTransformations = null;
            }
            return this;
        }

        public DynamicModelHolderDrawEvent modelHolder(DynamicModelHolder model) {
            this.dynamicModelHolder = model;
            return this;
        }

        protected DynamicModelHolderDrawEvent setMatrices(StoredMatrixTransformations m) {
            this.storedMatrixTransformations = m;
            return this;
        }

        public DynamicModelHolder getDynamicModelHolder() {
            return dynamicModelHolder;
        }

        @Override
        public void run(World world, DynamicModelHolderDrawEvent self, StoredMatrixTransformations storedMatrixTransformations, StoredMatrixTransformations matrices, Direction facing, int light) {
            parent.run(world, self, storedMatrixTransformations.copy(), matrices.copy(), facing, light);
        }
    }*/
    
    @FunctionalInterface
    public interface DrawEvent {
        void run(World world, StoredMatrixTransformations storedMatrixTransformations, Direction facing, int light);
    }
}
