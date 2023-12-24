package pro.mikey.dfuoptimize;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(DFUOptimize.MOD_ID)
public class DFUOptimize {
	public static final String MOD_ID = "dfuoptimize";
	public static final String PREFIX_GUI = String.format("%s:textures/gui/", MOD_ID);

	public static Logger logger = LogManager.getLogger();

	public DFUOptimize() {
		ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> "", (c, b) -> true));
		DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientController::setup);
	}
}
