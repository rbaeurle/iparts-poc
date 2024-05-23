/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.messaging.text;

import de.docware.apps.etk.base.importer.base.model.fixedlength.FixedLenFieldDescription;
import de.docware.apps.etk.base.importer.base.model.fixedlength.FixedLenRecordType;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsFactoryDataTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog.*;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Helfer zum Mappen von DIALOG-Typen auf Importer und FixedLength Definitionen
 */
public class iPartsTextToDIALOGMapper {

    private static final FixedLenFieldDescription[] HEADER_ATTRIBUTES = new FixedLenFieldDescription[]{
            new FixedLenFieldDescription(62, 62, iPartsTransferConst.ATTR_TABLE_SDB_FLAG),
            new FixedLenFieldDescription(14, 32, iPartsTransferConst.ATTR_TABLE_KEM)
    };

    private static final Map<String, FixedLenRecordType[]> RECORD_TYPE_DEFINITIONS = new HashMap<>();
    private static final Map<String, DialogDirectImportFunction> IMPORTER_MAP = new HashMap<>();

    static {
        addDefinition(CodeMasterDataImporter.DIALOG_TABLENAME,
                      new FixedLenFieldDescription(73, 96, CodeMasterDataImporter.RES_SN),
                      new FixedLenFieldDescription(97, 99, CodeMasterDataImporter.RES_SPS),
                      new FixedLenFieldDescription(100, 114, CodeMasterDataImporter.RES_SDA),
                      new FixedLenFieldDescription(115, 129, CodeMasterDataImporter.RES_SDB),
                      new FixedLenFieldDescription(130, 130, CodeMasterDataImporter.RES_PGKZ),
                      new FixedLenFieldDescription(164, 213, CodeMasterDataImporter.RES_BEN));

        addDefinition(ColorTableImporter.DIALOG_TABLENAME,
                      new FixedLenFieldDescription(73, 96, ColorTableImporter.FTS_FT),
                      new FixedLenFieldDescription(97, 99, ColorTableImporter.FTS_SPS),
                      new FixedLenFieldDescription(100, 114, ColorTableImporter.FTS_SDA),
                      new FixedLenFieldDescription(115, 129, ColorTableImporter.FTS_SDB),
                      new FixedLenFieldDescription(130, 179, ColorTableImporter.FTS_BEN),
                      new FixedLenFieldDescription(184, 333, ColorTableImporter.FTS_BEM),
                      new FixedLenFieldDescription(334, 335, ColorTableImporter.FTS_FIKZ));

        addDefinition(MasterDataDialogSeriesImporter.DIALOG_TABLENAME,
                      new FixedLenFieldDescription(73, 96, MasterDataDialogSeriesImporter.BRS_BR),
                      new FixedLenFieldDescription(97, 99, MasterDataDialogSeriesImporter.BRS_SPS),
                      new FixedLenFieldDescription(100, 114, MasterDataDialogSeriesImporter.BRS_SDA),
                      new FixedLenFieldDescription(115, 129, MasterDataDialogSeriesImporter.BRS_SDB),
                      new FixedLenFieldDescription(130, 130, MasterDataDialogSeriesImporter.BRS_PGKZ),
                      new FixedLenFieldDescription(131, 131, MasterDataDialogSeriesImporter.BRS_AKZ),
                      new FixedLenFieldDescription(136, 137, MasterDataDialogSeriesImporter.BRS_SNRKZ),
                      new FixedLenFieldDescription(138, 187, MasterDataDialogSeriesImporter.BRS_BEN),
                      new FixedLenFieldDescription(188, 188, MasterDataDialogSeriesImporter.BRS_ETKZ),
                      new FixedLenFieldDescription(343, 343, MasterDataDialogSeriesImporter.BRS_ERKZ));

        addDefinition(MasterDataDialogModelImporter.DIALOG_TABLENAME,
                      new FixedLenFieldDescription(73, 96, MasterDataDialogModelImporter.BMS_SNR),
                      new FixedLenFieldDescription(97, 99, MasterDataDialogModelImporter.BMS_SPS),
                      new FixedLenFieldDescription(100, 114, MasterDataDialogModelImporter.BMS_SDA),
                      new FixedLenFieldDescription(115, 129, MasterDataDialogModelImporter.BMS_SDB),
                      new FixedLenFieldDescription(142, 144, MasterDataDialogModelImporter.BMS_PS),
                      new FixedLenFieldDescription(145, 147, MasterDataDialogModelImporter.BMS_KW),
                      new FixedLenFieldDescription(148, 177, MasterDataDialogModelImporter.BMS_VBEZ),
                      new FixedLenFieldDescription(178, 227, MasterDataDialogModelImporter.BMS_BEN),
                      new FixedLenFieldDescription(580, 594, MasterDataDialogModelImporter.BMS_KON_BEZ),
                      new FixedLenFieldDescription(615, 615, MasterDataDialogModelImporter.BMS_UNG),
                      new FixedLenFieldDescription(616, 618, MasterDataDialogModelImporter.BMS_ANT_ART),
                      new FixedLenFieldDescription(619, 628, MasterDataDialogModelImporter.BMS_MOT_KON),
                      new FixedLenFieldDescription(629, 630, MasterDataDialogModelImporter.BMS_ANZ_ZYL),
                      new FixedLenFieldDescription(631, 631, MasterDataDialogModelImporter.BMS_MOT_ART));

        addDefinition(ColorNumberImporter.DIALOG_TABLENAME,
                      new FixedLenFieldDescription(73, 76, ColorNumberImporter.FNR_FNR),
                      new FixedLenFieldDescription(77, 79, ColorNumberImporter.FNR_SPS),
                      new FixedLenFieldDescription(95, 109, ColorNumberImporter.FNR_SDB),
                      new FixedLenFieldDescription(110, 159, ColorNumberImporter.FNR_BEN));

        addDefinition(MasterDataDialogImporter.DIALOG_TABLENAME_TS1,
                      new FixedLenFieldDescription(77, 100, MasterDataDialogImporter.TS1_TEIL),
                      new FixedLenFieldDescription(101, 115, MasterDataDialogImporter.TS1_SDA),
                      new FixedLenFieldDescription(116, 130, MasterDataDialogImporter.TS1_SDB),
                      new FixedLenFieldDescription(165, 165, MasterDataDialogImporter.TS1_DOKKZ),
                      new FixedLenFieldDescription(166, 167, MasterDataDialogImporter.TS1_EHM),
                      new FixedLenFieldDescription(168, 168, MasterDataDialogImporter.TS1_ETKZ),
                      new FixedLenFieldDescription(171, 171, MasterDataDialogImporter.TS1_FARKZ),
                      new FixedLenFieldDescription(172, 173, MasterDataDialogImporter.TS1_FGST),
                      new FixedLenFieldDescription(174, 180, MasterDataDialogImporter.TS1_FGW1),
                      new FixedLenFieldDescription(251, 274, MasterDataDialogImporter.TS1_ZBEZUG),
                      new FixedLenFieldDescription(276, 277, MasterDataDialogImporter.TS1_ZBKZ),
                      new FixedLenFieldDescription(278, 285, MasterDataDialogImporter.TS1_ZDATUM),
                      new FixedLenFieldDescription(290, 293, MasterDataDialogImporter.TS1_ZGS),
                      new FixedLenFieldDescription(294, 317, MasterDataDialogImporter.TS1_ZSIEHE),
                      new FixedLenFieldDescription(443, 452, MasterDataDialogImporter.TS1_EATTR),
                      new FixedLenFieldDescription(453, 457, MasterDataDialogImporter.TS1_VERKSNR));

        addDefinition(MasterDataDialogImporter.DIALOG_TABLENAME_TS2,
                      new FixedLenFieldDescription(77, 100, MasterDataDialogImporter.TS2_TEIL),
                      new FixedLenFieldDescription(101, 103, MasterDataDialogImporter.TS2_SPS),
                      new FixedLenFieldDescription(104, 118, MasterDataDialogImporter.TS2_SDA),
                      new FixedLenFieldDescription(119, 133, MasterDataDialogImporter.TS2_SDB),
                      new FixedLenFieldDescription(134, 183, MasterDataDialogImporter.TS2_BEN));

        addDefinition(MasterDataDialogImporter.DIALOG_TABLENAME_TS6,
                      new FixedLenFieldDescription(73, 96, MasterDataDialogImporter.TS6_TEIL),
                      new FixedLenFieldDescription(97, 111, MasterDataDialogImporter.TS6_SDA),
                      new FixedLenFieldDescription(112, 126, MasterDataDialogImporter.TS6_SDB),
                      new FixedLenFieldDescription(234, 361, MasterDataDialogImporter.TS6_WEZ));

        addDefinition(MasterDataDialogImporter.DIALOG_TABLENAME_GEWS,
                      new FixedLenFieldDescription(73, 96, MasterDataDialogImporter.GEWS_TEIL),
                      new FixedLenFieldDescription(97, 100, MasterDataDialogImporter.GEWS_ZGS),
                      new FixedLenFieldDescription(116, 130, MasterDataDialogImporter.GEWS_SDB),
                      new FixedLenFieldDescription(139, 145, MasterDataDialogImporter.GEWS_PROGEW),
                      new FixedLenFieldDescription(146, 152, MasterDataDialogImporter.GEWS_GEWGEW));

        addDefinition(DSRDataImporter.DIALOG_TABLENAME,
                      new FixedLenFieldDescription(73, 96, DSRDataImporter.TMK_TEIL),
                      new FixedLenFieldDescription(97, 111, DSRDataImporter.TMK_TYP),
                      new FixedLenFieldDescription(112, 114, DSRDataImporter.TMK_NR),
                      new FixedLenFieldDescription(115, 117, DSRDataImporter.TMK_SPS),
                      new FixedLenFieldDescription(118, 132, DSRDataImporter.TMK_SDA),
                      new FixedLenFieldDescription(133, 147, DSRDataImporter.TMK_SDB),
                      new FixedLenFieldDescription(148, 247, DSRDataImporter.TMK_MK1),
                      new FixedLenFieldDescription(248, 248, DSRDataImporter.TMK_MK2),
                      new FixedLenFieldDescription(249, 348, DSRDataImporter.TMK_MK3),
                      new FixedLenFieldDescription(349, 353, DSRDataImporter.TMK_MK4),
                      new FixedLenFieldDescription(354, 453, DSRDataImporter.TMK_MK5),
                      new FixedLenFieldDescription(458, 757, DSRDataImporter.TMK_MK6),
                      new FixedLenFieldDescription(762, 1061, DSRDataImporter.TMK_MK7),
                      new FixedLenFieldDescription(1066, 1365, DSRDataImporter.TMK_TEXT),
                      new FixedLenFieldDescription(1366, 1375, DSRDataImporter.TMK_ID));

        addDefinition(HmMSmStructureImporter.DIALOG_TABLENAME,
                      new FixedLenFieldDescription(73, 96, HmMSmStructureImporter.KGVZ_BRRA),
                      new FixedLenFieldDescription(97, 99, HmMSmStructureImporter.KGVZ_SPS),
                      new FixedLenFieldDescription(100, 114, HmMSmStructureImporter.KGVZ_SDATA),
                      new FixedLenFieldDescription(115, 129, HmMSmStructureImporter.KGVZ_SDATB),
                      new FixedLenFieldDescription(130, 179, HmMSmStructureImporter.KGVZ_BEN),
                      new FixedLenFieldDescription(180, 219, HmMSmStructureImporter.KGVZ_VZWK),
                      new FixedLenFieldDescription(220, 220, HmMSmStructureImporter.KGVZ_GHM),
                      new FixedLenFieldDescription(221, 221, HmMSmStructureImporter.KGVZ_GHS),
                      new FixedLenFieldDescription(222, 224, HmMSmStructureImporter.KGVZ_KGU),
                      new FixedLenFieldDescription(225, 225, HmMSmStructureImporter.KGVZ_PRI),
                      new FixedLenFieldDescription(226, 226, HmMSmStructureImporter.KGVZ_VERTRIEB_KZ));

        addDefinition(PartListDataImporter.BRTE_PREFIX,
                      new FixedLenFieldDescription(73, 73, addPrefix(PartListDataImporter.PG, PartListDataImporter.BRTE_PREFIX)),
                      new FixedLenFieldDescription(74, 80, addPrefix(PartListDataImporter.BR, PartListDataImporter.BRTE_PREFIX)),
                      new FixedLenFieldDescription(81, 86, addPrefix(PartListDataImporter.RAS, PartListDataImporter.BRTE_PREFIX)),
                      new FixedLenFieldDescription(87, 90, addPrefix(PartListDataImporter.POSE, PartListDataImporter.BRTE_PREFIX)),
                      new FixedLenFieldDescription(91, 94, addPrefix(PartListDataImporter.SESI, PartListDataImporter.BRTE_PREFIX)),
                      new FixedLenFieldDescription(95, 96, addPrefix(PartListDataImporter.POSP, PartListDataImporter.BRTE_PREFIX)),
                      new FixedLenFieldDescription(97, 100, addPrefix(PartListDataImporter.PV, PartListDataImporter.BRTE_PREFIX)),
                      new FixedLenFieldDescription(101, 102, addPrefix(PartListDataImporter.WW, PartListDataImporter.BRTE_PREFIX)),
                      new FixedLenFieldDescription(103, 105, addPrefix(PartListDataImporter.ETZ, PartListDataImporter.BRTE_PREFIX)),
                      new FixedLenFieldDescription(106, 110, addPrefix(PartListDataImporter.AA, PartListDataImporter.BRTE_PREFIX)),
                      new FixedLenFieldDescription(111, 125, addPrefix(PartListDataImporter.SDATA, PartListDataImporter.BRTE_PREFIX)),
                      new FixedLenFieldDescription(126, 140, addPrefix(PartListDataImporter.SDATB, PartListDataImporter.BRTE_PREFIX)),
                      new FixedLenFieldDescription(141, 153, addPrefix(PartListDataImporter.KEMA, PartListDataImporter.BRTE_PREFIX)),
                      new FixedLenFieldDescription(154, 166, addPrefix(PartListDataImporter.KEMB, PartListDataImporter.BRTE_PREFIX)),
                      new FixedLenFieldDescription(167, 171, addPrefix(PartListDataImporter.STEUA, PartListDataImporter.BRTE_PREFIX)),
                      new FixedLenFieldDescription(172, 176, addPrefix(PartListDataImporter.STEUB, PartListDataImporter.BRTE_PREFIX)),
                      new FixedLenFieldDescription(177, 178, addPrefix(PartListDataImporter.FED, PartListDataImporter.BRTE_PREFIX)),
                      new FixedLenFieldDescription(179, 202, addPrefix(PartListDataImporter.TEIL, PartListDataImporter.BRTE_PREFIX)),
                      new FixedLenFieldDescription(203, 203, addPrefix(PartListDataImporter.L, PartListDataImporter.BRTE_PREFIX)),
                      new FixedLenFieldDescription(204, 204, addPrefix(PartListDataImporter.MGKZ, PartListDataImporter.BRTE_PREFIX)),
                      new FixedLenFieldDescription(205, 211, addPrefix(PartListDataImporter.MG, PartListDataImporter.BRTE_PREFIX)),
                      new FixedLenFieldDescription(212, 218, addPrefix(PartListDataImporter.RFMEA, PartListDataImporter.BRTE_PREFIX)),
                      new FixedLenFieldDescription(219, 225, addPrefix(PartListDataImporter.RFMEN, PartListDataImporter.BRTE_PREFIX)),
                      new FixedLenFieldDescription(226, 233, addPrefix(PartListDataImporter.PTE, PartListDataImporter.BRTE_PREFIX)),
                      new FixedLenFieldDescription(234, 238, addPrefix(PartListDataImporter.KGUM, PartListDataImporter.BRTE_PREFIX)),
                      new FixedLenFieldDescription(239, 240, addPrefix(PartListDataImporter.STR, PartListDataImporter.BRTE_PREFIX)),
                      new FixedLenFieldDescription(241, 241, addPrefix(PartListDataImporter.RFG, PartListDataImporter.BRTE_PREFIX)),
                      new FixedLenFieldDescription(242, 373, addPrefix(PartListDataImporter.VERT, PartListDataImporter.BRTE_PREFIX)),
                      new FixedLenFieldDescription(374, 374, addPrefix(PartListDataImporter.ZBKZ, PartListDataImporter.BRTE_PREFIX)),
                      new FixedLenFieldDescription(375, 375, addPrefix(PartListDataImporter.VARG, PartListDataImporter.BRTE_PREFIX)),
                      new FixedLenFieldDescription(376, 376, addPrefix(PartListDataImporter.VARM, PartListDataImporter.BRTE_PREFIX)),
                      new FixedLenFieldDescription(377, 382, addPrefix(PartListDataImporter.GES, PartListDataImporter.BRTE_PREFIX)),
                      new FixedLenFieldDescription(383, 395, addPrefix(PartListDataImporter.PROJ, PartListDataImporter.BRTE_PREFIX)),
                      new FixedLenFieldDescription(396, 397, addPrefix(PartListDataImporter.ETKZ, PartListDataImporter.BRTE_PREFIX)),
                      new FixedLenFieldDescription(398, 652, addPrefix(PartListDataImporter.BZAE_NEU, PartListDataImporter.BRTE_PREFIX)),
                      new FixedLenFieldDescription(653, 682, PartListDataImporter.BRTE_EREIA),
                      new FixedLenFieldDescription(683, 712, PartListDataImporter.BRTE_EREIB),
                      new FixedLenFieldDescription(717, 1316, addPrefix(PartListDataImporter.CR, PartListDataImporter.BRTE_PREFIX)));

        addDefinition(PartListTextDataImporter.DIALOG_TABLENAME,
                      new FixedLenFieldDescription(73, 73, PartListTextDataImporter.BCTX_PG),
                      new FixedLenFieldDescription(74, 80, PartListTextDataImporter.BCTX_BR),
                      new FixedLenFieldDescription(81, 86, PartListTextDataImporter.BCTX_RAS),
                      new FixedLenFieldDescription(87, 90, PartListTextDataImporter.BCTX_POSE),
                      new FixedLenFieldDescription(101, 104, PartListTextDataImporter.BCTX_PV),
                      new FixedLenFieldDescription(105, 106, PartListTextDataImporter.BCTX_WW),
                      new FixedLenFieldDescription(107, 109, PartListTextDataImporter.BCTX_ETZ),
                      new FixedLenFieldDescription(110, 112, PartListTextDataImporter.BCTX_SPS),
                      new FixedLenFieldDescription(113, 127, PartListTextDataImporter.BCTX_SDATA),
                      new FixedLenFieldDescription(128, 142, PartListTextDataImporter.BCTX_SDATB),
                      new FixedLenFieldDescription(143, 144, PartListTextDataImporter.BCTX_FED),
                      new FixedLenFieldDescription(145, 194, PartListTextDataImporter.BCTX_AATAB),
                      new FixedLenFieldDescription(195, 196, PartListTextDataImporter.BCTX_STR),
                      new FixedLenFieldDescription(199, 200, PartListTextDataImporter.BCTX_TXTART),
                      new FixedLenFieldDescription(201, 202, PartListTextDataImporter.BCTX_FS),
                      new FixedLenFieldDescription(203, 247, PartListTextDataImporter.BCTX_TEXT),
                      new FixedLenFieldDescription(248, 248, PartListTextDataImporter.BCTX_RFG));

        addDefinition(GenericPartImporter.DIALOG_TABLENAME,
                      new FixedLenFieldDescription(73, 79, GenericPartImporter.BCTG_BR),
                      new FixedLenFieldDescription(80, 85, GenericPartImporter.BCTG_RAS),
                      new FixedLenFieldDescription(86, 89, GenericPartImporter.BCTG_POSE),
                      new FixedLenFieldDescription(90, 93, GenericPartImporter.BCTG_SESI),
                      new FixedLenFieldDescription(94, 95, GenericPartImporter.BCTG_POSP),
                      new FixedLenFieldDescription(96, 99, GenericPartImporter.BCTG_PV),
                      new FixedLenFieldDescription(100, 101, GenericPartImporter.BCTG_WW),
                      new FixedLenFieldDescription(102, 104, GenericPartImporter.BCTG_ETZ),
                      new FixedLenFieldDescription(105, 109, GenericPartImporter.BCTG_AA),
                      new FixedLenFieldDescription(110, 124, GenericPartImporter.BCTG_SDA),
                      new FixedLenFieldDescription(125, 139, GenericPartImporter.BCTG_SDB),
                      new FixedLenFieldDescription(140, 163, GenericPartImporter.BCTG_TEIL),
                      new FixedLenFieldDescription(164, 172, GenericPartImporter.BCTG_GP),
                      new FixedLenFieldDescription(173, 175, GenericPartImporter.BCTG_VNR),
                      new FixedLenFieldDescription(176, 177, GenericPartImporter.BCTG_SOLUTION));

        addDefinition(DIALOGPosTextImporter.DIALOG_TABLENAME,
                      new FixedLenFieldDescription(73, 79, DIALOGPosTextImporter.POSX_BR),
                      new FixedLenFieldDescription(80, 85, DIALOGPosTextImporter.POSX_RAS),
                      new FixedLenFieldDescription(86, 89, DIALOGPosTextImporter.POSX_POSE),
                      new FixedLenFieldDescription(90, 93, DIALOGPosTextImporter.POSX_SESI),
                      new FixedLenFieldDescription(94, 96, DIALOGPosTextImporter.POSX_SPS),
                      new FixedLenFieldDescription(97, 111, DIALOGPosTextImporter.POSX_SDA),
                      new FixedLenFieldDescription(112, 126, DIALOGPosTextImporter.POSX_SDB),
                      new FixedLenFieldDescription(127, 176, DIALOGPosTextImporter.POSX_BEN));

        // Alte Definition für POS (Payload: 132 Zeichen)
        addDefinition(iPartsTextToDIALOGDataHelper.makeAlternativeDefinitionKey(GenericInstallLocationImporter.DIALOG_TABLENAME),
                      new FixedLenFieldDescription(73, 79, GenericInstallLocationImporter.POS_BR),
                      new FixedLenFieldDescription(80, 85, GenericInstallLocationImporter.POS_RAS),
                      new FixedLenFieldDescription(86, 89, GenericInstallLocationImporter.POS_POSE),
                      new FixedLenFieldDescription(90, 93, GenericInstallLocationImporter.POS_SESI),
                      new FixedLenFieldDescription(94, 108, GenericInstallLocationImporter.POS_SDA),
                      new FixedLenFieldDescription(109, 123, GenericInstallLocationImporter.POS_SDB),
                      new FixedLenFieldDescription(124, 125, GenericInstallLocationImporter.POS_FED),
                      new FixedLenFieldDescription(126, 127, GenericInstallLocationImporter.POS_STR),
                      new FixedLenFieldDescription(128, 140, GenericInstallLocationImporter.POS_PS),
                      new FixedLenFieldDescription(141, 141, GenericInstallLocationImporter.POS_MK_KZ),
                      new FixedLenFieldDescription(142, 142, GenericInstallLocationImporter.POS_PETK),
                      new FixedLenFieldDescription(143, 143, GenericInstallLocationImporter.POS_PWK_KZ),
                      new FixedLenFieldDescription(144, 144, GenericInstallLocationImporter.POS_PTK_KZ),
                      new FixedLenFieldDescription(145, 184, GenericInstallLocationImporter.POS_ITEXT),
                      new FixedLenFieldDescription(185, 185, GenericInstallLocationImporter.POS_LOEKZ),
                      new FixedLenFieldDescription(186, 186, GenericInstallLocationImporter.POS_SPLIT),
                      new FixedLenFieldDescription(187, 204, GenericInstallLocationImporter.POS_GEN_VO));

        // Neue Definition für POS (Payload: 133 Zeichen)
        addDefinition(GenericInstallLocationImporter.DIALOG_TABLENAME,
                      new FixedLenFieldDescription(73, 79, GenericInstallLocationImporter.POS_BR),
                      new FixedLenFieldDescription(80, 85, GenericInstallLocationImporter.POS_RAS),
                      new FixedLenFieldDescription(86, 89, GenericInstallLocationImporter.POS_POSE),
                      new FixedLenFieldDescription(90, 93, GenericInstallLocationImporter.POS_SESI),
                      new FixedLenFieldDescription(94, 108, GenericInstallLocationImporter.POS_SDA),
                      new FixedLenFieldDescription(109, 123, GenericInstallLocationImporter.POS_SDB),
                      new FixedLenFieldDescription(124, 125, GenericInstallLocationImporter.POS_FED),
                      new FixedLenFieldDescription(126, 127, GenericInstallLocationImporter.POS_STR),
                      new FixedLenFieldDescription(128, 140, GenericInstallLocationImporter.POS_PS),
                      new FixedLenFieldDescription(141, 141, GenericInstallLocationImporter.POS_MK_KZ),
                      new FixedLenFieldDescription(142, 142, GenericInstallLocationImporter.POS_PETK),
                      new FixedLenFieldDescription(143, 143, GenericInstallLocationImporter.POS_PWK_KZ),
                      new FixedLenFieldDescription(144, 144, GenericInstallLocationImporter.POS_PTK_KZ),
                      new FixedLenFieldDescription(145, 184, GenericInstallLocationImporter.POS_ITEXT),
                      new FixedLenFieldDescription(185, 185, GenericInstallLocationImporter.POS_LOEKZ),
                      new FixedLenFieldDescription(186, 187, GenericInstallLocationImporter.POS_SPLIT),
                      new FixedLenFieldDescription(188, 205, GenericInstallLocationImporter.POS_GEN_VO));

        addDefinition(FactoryDataImporter.WBRT_PREFIX,
                      new FixedLenFieldDescription(73, 76, addPrefix(FactoryDataImporter.WK, FactoryDataImporter.WBRT_PREFIX)),
                      new FixedLenFieldDescription(77, 77, addPrefix(FactoryDataImporter.PG, FactoryDataImporter.WBRT_PREFIX)),
                      new FixedLenFieldDescription(78, 84, addPrefix(FactoryDataImporter.BR, FactoryDataImporter.WBRT_PREFIX)),
                      new FixedLenFieldDescription(85, 90, addPrefix(FactoryDataImporter.RAS, FactoryDataImporter.WBRT_PREFIX)),
                      new FixedLenFieldDescription(91, 94, addPrefix(FactoryDataImporter.POSE, FactoryDataImporter.WBRT_PREFIX)),
                      new FixedLenFieldDescription(95, 98, addPrefix(FactoryDataImporter.SESI, FactoryDataImporter.WBRT_PREFIX)),
                      new FixedLenFieldDescription(101, 104, addPrefix(FactoryDataImporter.PV, FactoryDataImporter.WBRT_PREFIX)),
                      new FixedLenFieldDescription(105, 106, addPrefix(FactoryDataImporter.WW, FactoryDataImporter.WBRT_PREFIX)),
                      new FixedLenFieldDescription(107, 109, addPrefix(FactoryDataImporter.ETZ, FactoryDataImporter.WBRT_PREFIX)),
                      new FixedLenFieldDescription(110, 114, addPrefix(FactoryDataImporter.AA, FactoryDataImporter.WBRT_PREFIX)),
                      new FixedLenFieldDescription(115, 129, addPrefix(FactoryDataImporter.SDATA, FactoryDataImporter.WBRT_PREFIX)),
                      new FixedLenFieldDescription(130, 130, addPrefix(FactoryDataImporter.SPKZ, FactoryDataImporter.WBRT_PREFIX)),
                      new FixedLenFieldDescription(131, 131, addPrefix(FactoryDataImporter.SPKZ1, FactoryDataImporter.WBRT_PREFIX)),
                      new FixedLenFieldDescription(132, 146, addPrefix(FactoryDataImporter.ADAT, FactoryDataImporter.WBRT_PREFIX)),
                      new FixedLenFieldDescription(183, 190, addPrefix(FactoryDataImporter.PEMA, FactoryDataImporter.WBRT_PREFIX)),
                      new FixedLenFieldDescription(191, 198, addPrefix(FactoryDataImporter.PEMB, FactoryDataImporter.WBRT_PREFIX)),
                      new FixedLenFieldDescription(199, 208, addPrefix(FactoryDataImporter.PEMTA, FactoryDataImporter.WBRT_PREFIX)),
                      new FixedLenFieldDescription(209, 218, addPrefix(FactoryDataImporter.PEMTB, FactoryDataImporter.WBRT_PREFIX)),
                      new FixedLenFieldDescription(239, 242, addPrefix(FactoryDataImporter.STCA, FactoryDataImporter.WBRT_PREFIX)),
                      new FixedLenFieldDescription(243, 246, addPrefix(FactoryDataImporter.STCB, FactoryDataImporter.WBRT_PREFIX)),
                      new FixedLenFieldDescription(347, 946, addPrefix(FactoryDataImporter.CRN, FactoryDataImporter.WBRT_PREFIX)),
                      new FixedLenFieldDescription(296, 319, addPrefix(FactoryDataImporter.TEIL, FactoryDataImporter.WBRT_PREFIX)),
                      new FixedLenFieldDescription(1120, 1149, FactoryDataImporter.WBRT_EREIA),
                      new FixedLenFieldDescription(1150, 1179, FactoryDataImporter.WBRT_EREIB),
                      new FixedLenFieldDescription(1219, 1226, addPrefix(FactoryDataImporter.PEMA + FactoryDataImporter.WBXT_OLD_SUFFIX, FactoryDataImporter.WBRT_PREFIX)),
                      new FixedLenFieldDescription(1227, 1236, addPrefix(FactoryDataImporter.PEMTA + FactoryDataImporter.WBXT_OLD_SUFFIX, FactoryDataImporter.WBRT_PREFIX)),
                      new FixedLenFieldDescription(1237, 1240, addPrefix(FactoryDataImporter.STCA + FactoryDataImporter.WBXT_OLD_SUFFIX, FactoryDataImporter.WBRT_PREFIX)));

        addDefinition(iPartsFactoryDataTypes.COLORTABLE_PART.getDatasetValue(),
                      new FixedLenFieldDescription(73, 76, addPrefix(ColorTableFactoryDataImporter.WK, iPartsFactoryDataTypes.COLORTABLE_PART.getDatasetValue())),
                      new FixedLenFieldDescription(77, 100, addPrefix(ColorTableFactoryDataImporter.FT, iPartsFactoryDataTypes.COLORTABLE_PART.getDatasetValue())),
                      new FixedLenFieldDescription(105, 108, addPrefix(ColorTableFactoryDataImporter.POS, iPartsFactoryDataTypes.COLORTABLE_PART.getDatasetValue())),
                      new FixedLenFieldDescription(109, 123, addPrefix(ColorTableFactoryDataImporter.SDA, iPartsFactoryDataTypes.COLORTABLE_PART.getDatasetValue())),
                      new FixedLenFieldDescription(124, 138, addPrefix(ColorTableFactoryDataImporter.ADAT, iPartsFactoryDataTypes.COLORTABLE_PART.getDatasetValue())),
                      new FixedLenFieldDescription(139, 153, addPrefix(ColorTableFactoryDataImporter.SDB, iPartsFactoryDataTypes.COLORTABLE_PART.getDatasetValue())),
                      new FixedLenFieldDescription(154, 177, addPrefix(ColorTableFactoryDataImporter.TEIL, iPartsFactoryDataTypes.COLORTABLE_PART.getDatasetValue())),
                      new FixedLenFieldDescription(178, 179, addPrefix(ColorTableFactoryDataImporter.FIKZ, iPartsFactoryDataTypes.COLORTABLE_PART.getDatasetValue())),
                      new FixedLenFieldDescription(180, 187, addPrefix(ColorTableFactoryDataImporter.PEMA, iPartsFactoryDataTypes.COLORTABLE_PART.getDatasetValue())),
                      new FixedLenFieldDescription(188, 195, addPrefix(ColorTableFactoryDataImporter.PEMB, iPartsFactoryDataTypes.COLORTABLE_PART.getDatasetValue())),
                      new FixedLenFieldDescription(196, 205, addPrefix(ColorTableFactoryDataImporter.PEMTA, iPartsFactoryDataTypes.COLORTABLE_PART.getDatasetValue())),
                      new FixedLenFieldDescription(206, 215, addPrefix(ColorTableFactoryDataImporter.PEMTB, iPartsFactoryDataTypes.COLORTABLE_PART.getDatasetValue())),
                      new FixedLenFieldDescription(216, 219, addPrefix(ColorTableFactoryDataImporter.STCA, iPartsFactoryDataTypes.COLORTABLE_PART.getDatasetValue())),
                      new FixedLenFieldDescription(220, 223, addPrefix(ColorTableFactoryDataImporter.STCB, iPartsFactoryDataTypes.COLORTABLE_PART.getDatasetValue())),
                      new FixedLenFieldDescription(225, 225, addPrefix(ColorTableFactoryDataImporter.FRGKZ1, iPartsFactoryDataTypes.COLORTABLE_PART.getDatasetValue())),
                      new FixedLenFieldDescription(265, 272, addPrefix(ColorTableFactoryDataImporter.PEMA + ColorTableFactoryDataImporter.WX9_WX10_OLD_SUFFIX, iPartsFactoryDataTypes.COLORTABLE_PART.getDatasetValue())),
                      new FixedLenFieldDescription(273, 282, addPrefix(ColorTableFactoryDataImporter.PEMTA + ColorTableFactoryDataImporter.WX9_WX10_OLD_SUFFIX, iPartsFactoryDataTypes.COLORTABLE_PART.getDatasetValue())),
                      new FixedLenFieldDescription(283, 286, addPrefix(ColorTableFactoryDataImporter.STCA + ColorTableFactoryDataImporter.WX9_WX10_OLD_SUFFIX, iPartsFactoryDataTypes.COLORTABLE_PART.getDatasetValue())));

        addDefinition(ColorTableFactoryDataImporter.WY9_PREFIX,
                      new FixedLenFieldDescription(73, 76, addPrefix(ColorTableFactoryDataImporter.WK, ColorTableFactoryDataImporter.WY9_PREFIX)),
                      new FixedLenFieldDescription(77, 100, addPrefix(ColorTableFactoryDataImporter.FT, ColorTableFactoryDataImporter.WY9_PREFIX)),
                      new FixedLenFieldDescription(105, 108, addPrefix(ColorTableFactoryDataImporter.POS, ColorTableFactoryDataImporter.WY9_PREFIX)),
                      new FixedLenFieldDescription(109, 123, addPrefix(ColorTableFactoryDataImporter.SDA, ColorTableFactoryDataImporter.WY9_PREFIX)),
                      new FixedLenFieldDescription(124, 138, addPrefix(ColorTableFactoryDataImporter.ADAT, ColorTableFactoryDataImporter.WY9_PREFIX)),
                      new FixedLenFieldDescription(139, 153, addPrefix(ColorTableFactoryDataImporter.SDB, ColorTableFactoryDataImporter.WY9_PREFIX)),
                      new FixedLenFieldDescription(159, 160, addPrefix(ColorTableFactoryDataImporter.FIKZ, ColorTableFactoryDataImporter.WY9_PREFIX)),
                      new FixedLenFieldDescription(161, 168, addPrefix(ColorTableFactoryDataImporter.PEMA, ColorTableFactoryDataImporter.WY9_PREFIX)),
                      new FixedLenFieldDescription(169, 176, addPrefix(ColorTableFactoryDataImporter.PEMB, ColorTableFactoryDataImporter.WY9_PREFIX)),
                      new FixedLenFieldDescription(177, 186, addPrefix(ColorTableFactoryDataImporter.PEMTA, ColorTableFactoryDataImporter.WY9_PREFIX)),
                      new FixedLenFieldDescription(187, 196, addPrefix(ColorTableFactoryDataImporter.PEMTB, ColorTableFactoryDataImporter.WY9_PREFIX)),
                      new FixedLenFieldDescription(197, 200, addPrefix(ColorTableFactoryDataImporter.STCA, ColorTableFactoryDataImporter.WY9_PREFIX)),
                      new FixedLenFieldDescription(201, 204, addPrefix(ColorTableFactoryDataImporter.STCB, ColorTableFactoryDataImporter.WY9_PREFIX)),
                      new FixedLenFieldDescription(810, 810, addPrefix(ColorTableFactoryDataImporter.FRGKZ1, ColorTableFactoryDataImporter.WY9_PREFIX)),
                      new FixedLenFieldDescription(811, 840, ColorTableFactoryDataImporter.WY9_EREIA),
                      new FixedLenFieldDescription(841, 870, ColorTableFactoryDataImporter.WY9_EREIB),
                      new FixedLenFieldDescription(910, 917, addPrefix(ColorTableFactoryDataImporter.PEMA + ColorTableFactoryDataImporter.WX9_WX10_OLD_SUFFIX, ColorTableFactoryDataImporter.WY9_PREFIX)),
                      new FixedLenFieldDescription(918, 927, addPrefix(ColorTableFactoryDataImporter.PEMTA + ColorTableFactoryDataImporter.WX9_WX10_OLD_SUFFIX, ColorTableFactoryDataImporter.WY9_PREFIX)),
                      new FixedLenFieldDescription(928, 931, addPrefix(ColorTableFactoryDataImporter.STCA + ColorTableFactoryDataImporter.WX9_WX10_OLD_SUFFIX, ColorTableFactoryDataImporter.WY9_PREFIX)));

        addDefinition(MasterDataDialogModelSeriesImporter.DIALOG_TABLENAME,
                      new FixedLenFieldDescription(73, 96, MasterDataDialogModelSeriesImporter.X2E_BR),
                      new FixedLenFieldDescription(97, 101, MasterDataDialogModelSeriesImporter.X2E_AA),
                      new FixedLenFieldDescription(102, 125, MasterDataDialogModelSeriesImporter.X2E_BMAA),
                      new FixedLenFieldDescription(126, 140, MasterDataDialogModelSeriesImporter.X2E_SDA),
                      new FixedLenFieldDescription(141, 155, MasterDataDialogModelSeriesImporter.X2E_SDB),
                      new FixedLenFieldDescription(156, 156, MasterDataDialogModelSeriesImporter.X2E_LK),
                      new FixedLenFieldDescription(157, 157, MasterDataDialogModelSeriesImporter.X2E_PGKZ),
                      new FixedLenFieldDescription(162, 311, MasterDataDialogModelSeriesImporter.X2E_CBED));

        addDefinition(SeriesCodesImporter.Y4E_PREFIX,
                      new FixedLenFieldDescription(73, 96, addPrefix(SeriesCodesImporter.BR, SeriesCodesImporter.Y4E_PREFIX)),
                      new FixedLenFieldDescription(97, 99, addPrefix(SeriesCodesImporter.GRP, SeriesCodesImporter.Y4E_PREFIX)),
                      new FixedLenFieldDescription(100, 103, addPrefix(SeriesCodesImporter.POS, SeriesCodesImporter.Y4E_PREFIX)),
                      new FixedLenFieldDescription(104, 107, addPrefix(SeriesCodesImporter.PV, SeriesCodesImporter.Y4E_PREFIX)),
                      new FixedLenFieldDescription(108, 112, addPrefix(SeriesCodesImporter.AA, SeriesCodesImporter.Y4E_PREFIX)),
                      new FixedLenFieldDescription(113, 127, addPrefix(SeriesCodesImporter.SDA, SeriesCodesImporter.Y4E_PREFIX)),
                      new FixedLenFieldDescription(128, 142, addPrefix(SeriesCodesImporter.SDB, SeriesCodesImporter.Y4E_PREFIX)),
                      new FixedLenFieldDescription(143, 166, addPrefix(SeriesCodesImporter.REEL, SeriesCodesImporter.Y4E_PREFIX)),
                      new FixedLenFieldDescription(167, 167, addPrefix(SeriesCodesImporter.LK, SeriesCodesImporter.Y4E_PREFIX)),
                      new FixedLenFieldDescription(168, 169, addPrefix(SeriesCodesImporter.CGKZ, SeriesCodesImporter.Y4E_PREFIX)),
                      new FixedLenFieldDescription(174, 773, addPrefix(SeriesCodesImporter.ZBED, SeriesCodesImporter.Y4E_PREFIX)),
                      new FixedLenFieldDescription(774, 774, addPrefix(SeriesCodesImporter.RFG, SeriesCodesImporter.Y4E_PREFIX)),
                      new FixedLenFieldDescription(775, 777, addPrefix(SeriesCodesImporter.MG, SeriesCodesImporter.Y4E_PREFIX)),
                      new FixedLenFieldDescription(778, 909, addPrefix(SeriesCodesImporter.VERT, SeriesCodesImporter.Y4E_PREFIX)),
                      new FixedLenFieldDescription(910, 911, addPrefix(SeriesCodesImporter.FED, SeriesCodesImporter.Y4E_PREFIX)),
                      new FixedLenFieldDescription(912, 912, addPrefix(SeriesCodesImporter.PGKZ, SeriesCodesImporter.Y4E_PREFIX)),
                      new FixedLenFieldDescription(917, 1516, addPrefix(SeriesCodesImporter.CBED, SeriesCodesImporter.Y4E_PREFIX)),
                      new FixedLenFieldDescription(1517, 1517, addPrefix(SeriesCodesImporter.BKZ, SeriesCodesImporter.Y4E_PREFIX)),
                      new FixedLenFieldDescription(1518, 1518, addPrefix(SeriesCodesImporter.PKZ, SeriesCodesImporter.Y4E_PREFIX)),
                      new FixedLenFieldDescription(1519, 1548, addPrefix(SeriesCodesImporter.EREIA, SeriesCodesImporter.Y4E_PREFIX)),
                      new FixedLenFieldDescription(1549, 1578, addPrefix(SeriesCodesImporter.EREIB, SeriesCodesImporter.Y4E_PREFIX)));

        addDefinition(EventDataImporter.DIALOG_TABLENAME,
                      new FixedLenFieldDescription(73, 79, EventDataImporter.EREI_BR),
                      new FixedLenFieldDescription(80, 109, EventDataImporter.EREI_ID),
                      new FixedLenFieldDescription(110, 112, EventDataImporter.EREI_SPS),
                      new FixedLenFieldDescription(113, 127, EventDataImporter.EREI_SDA),
                      new FixedLenFieldDescription(128, 142, EventDataImporter.EREI_SDB),
                      new FixedLenFieldDescription(143, 172, EventDataImporter.EREI_VG_ID),
                      new FixedLenFieldDescription(173, 222, EventDataImporter.EREI_BEN),
                      new FixedLenFieldDescription(223, 322, EventDataImporter.EREI_BEM),
                      new FixedLenFieldDescription(323, 323, EventDataImporter.EREI_KR),
                      new FixedLenFieldDescription(324, 324, EventDataImporter.EREI_STAT),
                      new FixedLenFieldDescription(325, 374, EventDataImporter.EREI_CR));

        addDefinition(KemMasterDataImporter.DIALOG_TABLENAME,
                      new FixedLenFieldDescription(73, 96, KemMasterDataImporter.KES_KEM),
                      new FixedLenFieldDescription(97, 99, KemMasterDataImporter.KES_SPS),
                      new FixedLenFieldDescription(100, 114, KemMasterDataImporter.KES_SDA),
                      new FixedLenFieldDescription(115, 129, KemMasterDataImporter.KES_SDB),
                      new FixedLenFieldDescription(130, 130, KemMasterDataImporter.KES_ASKZ),
                      new FixedLenFieldDescription(131, 132, KemMasterDataImporter.KES_VAKZ),
                      new FixedLenFieldDescription(133, 135, KemMasterDataImporter.KES_BAI),
                      new FixedLenFieldDescription(136, 136, KemMasterDataImporter.KES_GHM),
                      new FixedLenFieldDescription(137, 137, KemMasterDataImporter.KES_GHS),
                      new FixedLenFieldDescription(138, 152, KemMasterDataImporter.KES_ANTNR),
                      new FixedLenFieldDescription(153, 157, KemMasterDataImporter.KES_URSL),
                      new FixedLenFieldDescription(158, 217, KemMasterDataImporter.KES_BEN),
                      new FixedLenFieldDescription(218, 337, KemMasterDataImporter.KES_EV),
                      new FixedLenFieldDescription(338, 487, KemMasterDataImporter.KES_BEM),
                      new FixedLenFieldDescription(488, 488, KemMasterDataImporter.KES_GZ),
                      new FixedLenFieldDescription(489, 496, KemMasterDataImporter.KES_DGZ),
                      new FixedLenFieldDescription(497, 497, KemMasterDataImporter.KES_TRDS),
                      new FixedLenFieldDescription(498, 498, KemMasterDataImporter.KES_ZKDSW),
                      new FixedLenFieldDescription(499, 499, KemMasterDataImporter.KES_ABGAS1),
                      new FixedLenFieldDescription(500, 500, KemMasterDataImporter.KES_STKEMKZ),
                      new FixedLenFieldDescription(501, 514, KemMasterDataImporter.KES_STOPKEM),
                      new FixedLenFieldDescription(515, 527, KemMasterDataImporter.KES_AUFKEM),
                      new FixedLenFieldDescription(528, 535, KemMasterDataImporter.KES_ATERM),
                      new FixedLenFieldDescription(536, 543, KemMasterDataImporter.KES_VTERM),
                      new FixedLenFieldDescription(544, 557, KemMasterDataImporter.KES_ZM_KEM1),
                      new FixedLenFieldDescription(558, 571, KemMasterDataImporter.KES_ZM_KEM2),
                      new FixedLenFieldDescription(572, 585, KemMasterDataImporter.KES_ZM_KEM3),
                      new FixedLenFieldDescription(586, 599, KemMasterDataImporter.KES_ZM_KEM4),
                      new FixedLenFieldDescription(600, 601, KemMasterDataImporter.KES_ET_VAKZ),
                      new FixedLenFieldDescription(602, 615, KemMasterDataImporter.KES_ET_KEM),
                      new FixedLenFieldDescription(616, 627, KemMasterDataImporter.KES_ET_DATA),
                      new FixedLenFieldDescription(628, 639, KemMasterDataImporter.KES_ET_DATR),
                      new FixedLenFieldDescription(640, 640, KemMasterDataImporter.KES_ET_KZBT),
                      new FixedLenFieldDescription(641, 641, KemMasterDataImporter.KES_ET_KZSPR),
                      new FixedLenFieldDescription(646, 885, KemMasterDataImporter.KES_GRD),
                      new FixedLenFieldDescription(886, 888, KemMasterDataImporter.KES_AS),
                      new FixedLenFieldDescription(889, 889, KemMasterDataImporter.KES_TDAT),
                      new FixedLenFieldDescription(890, 890, KemMasterDataImporter.KES_SYS_KZ),
                      new FixedLenFieldDescription(891, 904, KemMasterDataImporter.KES_SKEM),
                      new FixedLenFieldDescription(905, 905, KemMasterDataImporter.KES_PRIO),
                      new FixedLenFieldDescription(906, 906, KemMasterDataImporter.KES_BEMUSTERUNG),
                      new FixedLenFieldDescription(907, 914, KemMasterDataImporter.KES_STERM),
                      new FixedLenFieldDescription(915, 922, KemMasterDataImporter.KES_ETERM),
                      new FixedLenFieldDescription(923, 925, KemMasterDataImporter.KES_DAUER));

        addDefinition(ZBVEDataImporter.DIALOG_TABLENAME,
                      new FixedLenFieldDescription(73, 96, ZBVEDataImporter.ZBVE_ZBV),
                      new FixedLenFieldDescription(97, 102, ZBVEDataImporter.ZBVE_POSE),
                      new FixedLenFieldDescription(108, 109, ZBVEDataImporter.ZBVE_WW),
                      new FixedLenFieldDescription(110, 124, ZBVEDataImporter.ZBVE_SDA),
                      new FixedLenFieldDescription(125, 139, ZBVEDataImporter.ZBVE_SDB),
                      new FixedLenFieldDescription(140, 163, ZBVEDataImporter.ZBVE_TEIL),
                      new FixedLenFieldDescription(164, 176, ZBVEDataImporter.ZBVE_KEMA),
                      new FixedLenFieldDescription(177, 189, ZBVEDataImporter.ZBVE_KEMB),
                      new FixedLenFieldDescription(190, 196, ZBVEDataImporter.ZBVE_MG),
                      new FixedLenFieldDescription(197, 199, ZBVEDataImporter.ZBVE_URS),
                      new FixedLenFieldDescription(200, 211, ZBVEDataImporter.ZBVE_BZAE));

        addDefinition(ColorTablePartOrContentImporter.Y9E_PREFIX,
                      new FixedLenFieldDescription(73, 96, addPrefix(ColorTablePartOrContentImporter.FT, ColorTablePartOrContentImporter.Y9E_PREFIX)),
                      new FixedLenFieldDescription(97, 100, addPrefix(ColorTablePartOrContentImporter.POS, ColorTablePartOrContentImporter.Y9E_PREFIX)),
                      new FixedLenFieldDescription(101, 115, addPrefix(ColorTablePartOrContentImporter.SDA, ColorTablePartOrContentImporter.Y9E_PREFIX)),
                      new FixedLenFieldDescription(116, 130, addPrefix(ColorTablePartOrContentImporter.SDB, ColorTablePartOrContentImporter.Y9E_PREFIX)),
                      new FixedLenFieldDescription(131, 134, addPrefix(ColorTablePartOrContentImporter.FARB, ColorTablePartOrContentImporter.Y9E_PREFIX)),
                      new FixedLenFieldDescription(135, 135, addPrefix(ColorTablePartOrContentImporter.PGKZ, ColorTablePartOrContentImporter.Y9E_PREFIX)),
                      new FixedLenFieldDescription(136, 137, addPrefix(ColorTablePartOrContentImporter.FIKZ, ColorTablePartOrContentImporter.Y9E_PREFIX)),
                      new FixedLenFieldDescription(142, 741, addPrefix(ColorTablePartOrContentImporter.CBED, ColorTablePartOrContentImporter.Y9E_PREFIX)),
                      new FixedLenFieldDescription(747, 776, ColorTablePartOrContentImporter.Y9E_EREIA),
                      new FixedLenFieldDescription(777, 806, ColorTablePartOrContentImporter.Y9E_EREIB));

        addDefinition(ColorTablePartOrContentImporter.X10E_PREFIX,
                      new FixedLenFieldDescription(73, 96, addPrefix(ColorTablePartOrContentImporter.FT, ColorTablePartOrContentImporter.X10E_PREFIX)),
                      new FixedLenFieldDescription(97, 100, addPrefix(ColorTablePartOrContentImporter.POS, ColorTablePartOrContentImporter.X10E_PREFIX)),
                      new FixedLenFieldDescription(101, 115, addPrefix(ColorTablePartOrContentImporter.SDA, ColorTablePartOrContentImporter.X10E_PREFIX)),
                      new FixedLenFieldDescription(116, 130, addPrefix(ColorTablePartOrContentImporter.SDB, ColorTablePartOrContentImporter.X10E_PREFIX)),
                      new FixedLenFieldDescription(131, 154, ColorTablePartOrContentImporter.X10E_TEIL),
                      new FixedLenFieldDescription(155, 156, addPrefix(ColorTablePartOrContentImporter.FIKZ, ColorTablePartOrContentImporter.X10E_PREFIX)));

        addDefinition(ReplacementPartsConstImporter.DIALOG_TABLENAME,
                      new FixedLenFieldDescription(77, 100, ReplacementPartsConstImporter.TS7_TEIL),
                      new FixedLenFieldDescription(101, 103, ReplacementPartsConstImporter.TS7_SPS),
                      new FixedLenFieldDescription(104, 118, ReplacementPartsConstImporter.TS7_SDA),
                      new FixedLenFieldDescription(119, 133, ReplacementPartsConstImporter.TS7_SDB),
                      new FixedLenFieldDescription(134, 193, ReplacementPartsConstImporter.TS7_WERKE),
                      new FixedLenFieldDescription(194, 200, ReplacementPartsConstImporter.TS7_RFME),
                      new FixedLenFieldDescription(201, 389, ReplacementPartsConstImporter.TS7_ATEXT),
                      new FixedLenFieldDescription(390, 413, ReplacementPartsConstImporter.TS7_VSNR),
                      new FixedLenFieldDescription(414, 437, ReplacementPartsConstImporter.TS7_NSNR),
                      new FixedLenFieldDescription(438, 438, ReplacementPartsConstImporter.TS7_VM),
                      new FixedLenFieldDescription(439, 439, ReplacementPartsConstImporter.TS7_WZ),
                      new FixedLenFieldDescription(440, 440, ReplacementPartsConstImporter.TS7_WS));

        addDefinition(VTNVDataImporter.DIALOG_TABLENAME,
                      new FixedLenFieldDescription(73, 96, VTNVDataImporter.VTNV_TEIL),
                      new FixedLenFieldDescription(97, 111, VTNVDataImporter.VTNV_SDATA),
                      new FixedLenFieldDescription(192, 192, VTNVDataImporter.VTNV_VOR_KZ_K),
                      new FixedLenFieldDescription(193, 216, VTNVDataImporter.VTNV_VOR_SNR),
                      new FixedLenFieldDescription(217, 223, VTNVDataImporter.VTNV_VOR_ET_RFME),
                      new FixedLenFieldDescription(224, 230, VTNVDataImporter.VTNV_AKT_ET_RFME),
                      new FixedLenFieldDescription(231, 254, VTNVDataImporter.VTNV_MIT_SNR1),
                      new FixedLenFieldDescription(255, 257, VTNVDataImporter.VTNV_MIT_MG1),
                      new FixedLenFieldDescription(258, 281, VTNVDataImporter.VTNV_MIT_SNR2),
                      new FixedLenFieldDescription(282, 284, VTNVDataImporter.VTNV_MIT_MG2),
                      new FixedLenFieldDescription(285, 308, VTNVDataImporter.VTNV_MIT_SNR3),
                      new FixedLenFieldDescription(309, 311, VTNVDataImporter.VTNV_MIT_MG3),
                      new FixedLenFieldDescription(312, 335, VTNVDataImporter.VTNV_MIT_SNR4),
                      new FixedLenFieldDescription(336, 338, VTNVDataImporter.VTNV_MIT_MG4),
                      new FixedLenFieldDescription(339, 362, VTNVDataImporter.VTNV_MIT_SNR5),
                      new FixedLenFieldDescription(363, 365, VTNVDataImporter.VTNV_MIT_MG5),
                      new FixedLenFieldDescription(366, 366, VTNVDataImporter.VTNV_SPERR_KZ),
                      new FixedLenFieldDescription(367, 367, VTNVDataImporter.VTNV_IDENT_ANFO));

        addDefinition(FootNoteMatRefImporter.DIALOG_TABLENAME,
                      new FixedLenFieldDescription(73, 96, FootNoteMatRefImporter.VTFN_TEIL),
                      new FixedLenFieldDescription(97, 102, FootNoteMatRefImporter.VTFN_FN));

        addDefinition(FootNotePosRefImporter.DIALOG_TABLENAME,
                      new FixedLenFieldDescription(73, 73, FootNotePosRefImporter.VBFN_PG),
                      new FixedLenFieldDescription(74, 80, FootNotePosRefImporter.VBFN_BR),
                      new FixedLenFieldDescription(81, 86, FootNotePosRefImporter.VBFN_MOD),
                      new FixedLenFieldDescription(87, 90, FootNotePosRefImporter.VBFN_POSE),
                      new FixedLenFieldDescription(91, 94, FootNotePosRefImporter.VBFN_SESI),
                      new FixedLenFieldDescription(95, 96, FootNotePosRefImporter.VBFN_POSP),
                      new FixedLenFieldDescription(97, 100, FootNotePosRefImporter.VBFN_PV),
                      new FixedLenFieldDescription(101, 102, FootNotePosRefImporter.VBFN_WW),
                      new FixedLenFieldDescription(103, 105, FootNotePosRefImporter.VBFN_ETZ),
                      new FixedLenFieldDescription(106, 110, FootNotePosRefImporter.VBFN_AA),
                      new FixedLenFieldDescription(111, 116, FootNotePosRefImporter.VBFN_FN),
                      new FixedLenFieldDescription(117, 131, FootNotePosRefImporter.VBFN_SDATA),
                      new FixedLenFieldDescription(132, 146, FootNotePosRefImporter.VBFN_SDATB));

        addDefinition(VFNDataImporter.DIALOG_TABLENAME,
                      new FixedLenFieldDescription(73, 78, VFNDataImporter.VFN_FNNR),
                      new FixedLenFieldDescription(82, 93, VFNDataImporter.VFN_DATUHR),
                      new FixedLenFieldDescription(103, 354, VFNDataImporter.VFN_FN));

        addDefinition(iPartsFactoryDataTypes.COLORTABLE_CONTENT_AS.getDatasetValue(),
                      new FixedLenFieldDescription(73, 96, addPrefix(ColorTableFactoryDataImporter.FT, iPartsFactoryDataTypes.COLORTABLE_CONTENT_AS.getDatasetValue())),
                      new FixedLenFieldDescription(97, 100, addPrefix(ColorTableFactoryDataImporter.POS, iPartsFactoryDataTypes.COLORTABLE_CONTENT_AS.getDatasetValue())),
                      new FixedLenFieldDescription(101, 104, addPrefix(ColorTableFactoryDataImporter.WERK, iPartsFactoryDataTypes.COLORTABLE_CONTENT_AS.getDatasetValue())),
                      new FixedLenFieldDescription(105, 119, addPrefix(ColorTableFactoryDataImporter.SDA, iPartsFactoryDataTypes.COLORTABLE_CONTENT_AS.getDatasetValue())),
                      new FixedLenFieldDescription(120, 134, addPrefix(ColorTableFactoryDataImporter.SDB, iPartsFactoryDataTypes.COLORTABLE_CONTENT_AS.getDatasetValue())),
                      new FixedLenFieldDescription(135, 142, addPrefix(ColorTableFactoryDataImporter.PEMA, iPartsFactoryDataTypes.COLORTABLE_CONTENT_AS.getDatasetValue())),
                      new FixedLenFieldDescription(143, 150, addPrefix(ColorTableFactoryDataImporter.PEMB, iPartsFactoryDataTypes.COLORTABLE_CONTENT_AS.getDatasetValue())),
                      new FixedLenFieldDescription(151, 160, addPrefix(ColorTableFactoryDataImporter.PEMTA, iPartsFactoryDataTypes.COLORTABLE_CONTENT_AS.getDatasetValue())),
                      new FixedLenFieldDescription(161, 170, addPrefix(ColorTableFactoryDataImporter.PEMTB, iPartsFactoryDataTypes.COLORTABLE_CONTENT_AS.getDatasetValue())),
                      new FixedLenFieldDescription(171, 175, addPrefix(ColorTableFactoryDataImporter.STCODEAB, iPartsFactoryDataTypes.COLORTABLE_CONTENT_AS.getDatasetValue())),
                      new FixedLenFieldDescription(176, 180, addPrefix(ColorTableFactoryDataImporter.STCODEBIS, iPartsFactoryDataTypes.COLORTABLE_CONTENT_AS.getDatasetValue())),
                      new FixedLenFieldDescription(181, 181, addPrefix(ColorTableFactoryDataImporter.ETKZ, iPartsFactoryDataTypes.COLORTABLE_CONTENT_AS.getDatasetValue())));

        addDefinition(iPartsFactoryDataTypes.COLORTABLE_PART_AS.getDatasetValue(),
                      new FixedLenFieldDescription(73, 96, addPrefix(ColorTableFactoryDataImporter.FT, iPartsFactoryDataTypes.COLORTABLE_PART_AS.getDatasetValue())),
                      new FixedLenFieldDescription(97, 100, addPrefix(ColorTableFactoryDataImporter.POS, iPartsFactoryDataTypes.COLORTABLE_PART_AS.getDatasetValue())),
                      new FixedLenFieldDescription(101, 104, addPrefix(ColorTableFactoryDataImporter.WERK, iPartsFactoryDataTypes.COLORTABLE_PART_AS.getDatasetValue())),
                      new FixedLenFieldDescription(105, 119, addPrefix(ColorTableFactoryDataImporter.SDA, iPartsFactoryDataTypes.COLORTABLE_PART_AS.getDatasetValue())),
                      new FixedLenFieldDescription(120, 134, addPrefix(ColorTableFactoryDataImporter.SDB, iPartsFactoryDataTypes.COLORTABLE_PART_AS.getDatasetValue())),
                      new FixedLenFieldDescription(135, 142, addPrefix(ColorTableFactoryDataImporter.PEMA, iPartsFactoryDataTypes.COLORTABLE_PART_AS.getDatasetValue())),
                      new FixedLenFieldDescription(143, 150, addPrefix(ColorTableFactoryDataImporter.PEMB, iPartsFactoryDataTypes.COLORTABLE_PART_AS.getDatasetValue())),
                      new FixedLenFieldDescription(151, 160, addPrefix(ColorTableFactoryDataImporter.PEMTA, iPartsFactoryDataTypes.COLORTABLE_PART_AS.getDatasetValue())),
                      new FixedLenFieldDescription(161, 170, addPrefix(ColorTableFactoryDataImporter.PEMTB, iPartsFactoryDataTypes.COLORTABLE_PART_AS.getDatasetValue())),
                      new FixedLenFieldDescription(171, 175, addPrefix(ColorTableFactoryDataImporter.STCODEAB, iPartsFactoryDataTypes.COLORTABLE_PART_AS.getDatasetValue())),
                      new FixedLenFieldDescription(176, 180, addPrefix(ColorTableFactoryDataImporter.STCODEBIS, iPartsFactoryDataTypes.COLORTABLE_PART_AS.getDatasetValue())),
                      new FixedLenFieldDescription(181, 181, addPrefix(ColorTableFactoryDataImporter.ETKZ, iPartsFactoryDataTypes.COLORTABLE_PART_AS.getDatasetValue())));

        addDefinition(FactoryDataImporter.VBW_PREFIX,
                      new FixedLenFieldDescription(73, 73, addPrefix(FactoryDataImporter.PG, FactoryDataImporter.VBW_PREFIX)),
                      new FixedLenFieldDescription(74, 80, addPrefix(FactoryDataImporter.BR, FactoryDataImporter.VBW_PREFIX)),
                      new FixedLenFieldDescription(81, 86, addPrefix(FactoryDataImporter.RAS, FactoryDataImporter.VBW_PREFIX)),
                      new FixedLenFieldDescription(87, 90, addPrefix(FactoryDataImporter.POSE, FactoryDataImporter.VBW_PREFIX)),
                      new FixedLenFieldDescription(91, 94, addPrefix(FactoryDataImporter.SESI, FactoryDataImporter.VBW_PREFIX)),
                      new FixedLenFieldDescription(97, 100, addPrefix(FactoryDataImporter.PV, FactoryDataImporter.VBW_PREFIX)),
                      new FixedLenFieldDescription(101, 102, addPrefix(FactoryDataImporter.WW, FactoryDataImporter.VBW_PREFIX)),
                      new FixedLenFieldDescription(103, 105, addPrefix(FactoryDataImporter.ETZ, FactoryDataImporter.VBW_PREFIX)),
                      new FixedLenFieldDescription(106, 109, FactoryDataImporter.VBW_WERK),
                      new FixedLenFieldDescription(110, 114, addPrefix(FactoryDataImporter.AA, FactoryDataImporter.VBW_PREFIX)),
                      new FixedLenFieldDescription(115, 129, addPrefix(FactoryDataImporter.SDATA, FactoryDataImporter.VBW_PREFIX)),
                      new FixedLenFieldDescription(145, 152, addPrefix(FactoryDataImporter.PEMA, FactoryDataImporter.VBW_PREFIX)),
                      new FixedLenFieldDescription(153, 160, addPrefix(FactoryDataImporter.PEMB, FactoryDataImporter.VBW_PREFIX)),
                      new FixedLenFieldDescription(161, 170, addPrefix(FactoryDataImporter.PEMTA, FactoryDataImporter.VBW_PREFIX)),
                      new FixedLenFieldDescription(171, 180, addPrefix(FactoryDataImporter.PEMTB, FactoryDataImporter.VBW_PREFIX)),
                      new FixedLenFieldDescription(181, 185, addPrefix(FactoryDataImporter.STCA, FactoryDataImporter.VBW_PREFIX)),
                      new FixedLenFieldDescription(186, 190, addPrefix(FactoryDataImporter.STCB, FactoryDataImporter.VBW_PREFIX)));

        addDefinition(SCTVDataImporter.DIALOG_TABLENAME,
                      new FixedLenFieldDescription(73, 79, SCTVDataImporter.SCTV_BR),
                      new FixedLenFieldDescription(80, 85, SCTVDataImporter.SCTV_RAS),
                      new FixedLenFieldDescription(86, 89, SCTVDataImporter.SCTV_POSE),
                      new FixedLenFieldDescription(90, 113, SCTVDataImporter.SCTV_TEIL),
                      new FixedLenFieldDescription(114, 118, SCTVDataImporter.SCTV_SCT),
                      new FixedLenFieldDescription(119, 133, SCTVDataImporter.SCTV_SDA),
                      new FixedLenFieldDescription(134, 148, SCTVDataImporter.SCTV_SDB),
                      new FixedLenFieldDescription(149, 149, SCTVDataImporter.SCTV_ORDNUNG),
                      new FixedLenFieldDescription(150, 157, SCTVDataImporter.SCTV_USERID));

        addDefinition(PartListAddDataImporter.VBRT_PREFIX,
                      new FixedLenFieldDescription(74, 80, addPrefix(PartListAddDataImporter.BR, PartListAddDataImporter.VBRT_PREFIX)),
                      new FixedLenFieldDescription(81, 86, addPrefix(PartListAddDataImporter.RAS, PartListAddDataImporter.VBRT_PREFIX)),
                      new FixedLenFieldDescription(87, 90, addPrefix(PartListAddDataImporter.POSE, PartListAddDataImporter.VBRT_PREFIX)),
                      new FixedLenFieldDescription(97, 100, addPrefix(PartListAddDataImporter.PV, PartListAddDataImporter.VBRT_PREFIX)),
                      new FixedLenFieldDescription(101, 102, addPrefix(PartListAddDataImporter.WW, PartListAddDataImporter.VBRT_PREFIX)),
                      new FixedLenFieldDescription(103, 105, addPrefix(PartListAddDataImporter.ETZ, PartListAddDataImporter.VBRT_PREFIX)),
                      new FixedLenFieldDescription(106, 120, addPrefix(PartListAddDataImporter.SDATA, PartListAddDataImporter.VBRT_PREFIX)),
                      new FixedLenFieldDescription(121, 135, addPrefix(PartListAddDataImporter.SDATB, PartListAddDataImporter.VBRT_PREFIX)),
                      new FixedLenFieldDescription(136, 148, addPrefix(PartListAddDataImporter.ERGK, PartListAddDataImporter.VBRT_PREFIX)),
                      new FixedLenFieldDescription(149, 186, addPrefix(PartListAddDataImporter.SPRN, PartListAddDataImporter.VBRT_PREFIX)),
                      new FixedLenFieldDescription(194, 195, addPrefix(PartListAddDataImporter.STR, PartListAddDataImporter.VBRT_PREFIX)),
                      new FixedLenFieldDescription(200, 799, addPrefix(PartListAddDataImporter.CODE, PartListAddDataImporter.VBRT_PREFIX)),
                      new FixedLenFieldDescription(800, 839, addPrefix(PartListAddDataImporter.ITEXT, PartListAddDataImporter.VBRT_PREFIX)),
                      new FixedLenFieldDescription(840, 869, PartListAddDataImporter.VBRT_EREIA),
                      new FixedLenFieldDescription(870, 899, PartListAddDataImporter.VBRT_EREIB));

        addDefinition(MasterDataDialogImporter.DIALOG_TABLENAME_VTNR,
                      new FixedLenFieldDescription(73, 96, MasterDataDialogImporter.VTNR_TEIL),
                      new FixedLenFieldDescription(112, 126, MasterDataDialogImporter.VTNR_SDATB),
                      new FixedLenFieldDescription(127, 141, MasterDataDialogImporter.VTNR_TBDT),
                      new FixedLenFieldDescription(142, 179, MasterDataDialogImporter.VTNR_SPRN),
                      new FixedLenFieldDescription(180, 180, MasterDataDialogImporter.VTNR_ERST),
                      new FixedLenFieldDescription(193, 194, MasterDataDialogImporter.VTNR_RECYKL),
                      new FixedLenFieldDescription(195, 234, MasterDataDialogImporter.VTNR_ITEXT),
                      new FixedLenFieldDescription(235, 266, MasterDataDialogImporter.VTNR_BNR));

        addDefinition(ResponseDataImporter.RMDA_PREFIX,
                      new FixedLenFieldDescription(73, 76, addPrefix(ResponseDataImporter.WN, ResponseDataImporter.RMDA_PREFIX)),
                      new FixedLenFieldDescription(77, 84, addPrefix(ResponseDataImporter.PEM, ResponseDataImporter.RMDA_PREFIX)),
                      new FixedLenFieldDescription(85, 108, addPrefix(ResponseDataImporter.BR, ResponseDataImporter.RMDA_PREFIX)),
                      new FixedLenFieldDescription(109, 113, addPrefix(ResponseDataImporter.AA, ResponseDataImporter.RMDA_PREFIX)),
                      new FixedLenFieldDescription(114, 137, addPrefix(ResponseDataImporter.BMAA, ResponseDataImporter.RMDA_PREFIX)),
                      new FixedLenFieldDescription(138, 155, addPrefix(ResponseDataImporter.FZGA, ResponseDataImporter.RMDA_PREFIX)),
                      new FixedLenFieldDescription(156, 170, addPrefix(ResponseDataImporter.ADAT, ResponseDataImporter.RMDA_PREFIX)),
                      new FixedLenFieldDescription(171, 171, addPrefix(ResponseDataImporter.L, ResponseDataImporter.RMDA_PREFIX)),
                      new FixedLenFieldDescription(202, 276, ResponseDataImporter.RMDA_TEXT),
                      new FixedLenFieldDescription(427, 427, ResponseDataImporter.RMDA_G));

        addDefinition(ResponseDataImporter.RMID_PREFIX,
                      new FixedLenFieldDescription(73, 76, addPrefix(ResponseDataImporter.WN, ResponseDataImporter.RMID_PREFIX)),
                      new FixedLenFieldDescription(77, 84, addPrefix(ResponseDataImporter.PEM, ResponseDataImporter.RMID_PREFIX)),
                      new FixedLenFieldDescription(85, 108, addPrefix(ResponseDataImporter.BR, ResponseDataImporter.RMID_PREFIX)),
                      new FixedLenFieldDescription(109, 113, addPrefix(ResponseDataImporter.AA, ResponseDataImporter.RMID_PREFIX)),
                      new FixedLenFieldDescription(114, 114, addPrefix(ResponseDataImporter.L, ResponseDataImporter.RMID_PREFIX)),
                      new FixedLenFieldDescription(115, 138, addPrefix(ResponseDataImporter.BMAA, ResponseDataImporter.RMID_PREFIX)),
                      new FixedLenFieldDescription(139, 156, addPrefix(ResponseDataImporter.FZGA, ResponseDataImporter.RMID_PREFIX)),
                      new FixedLenFieldDescription(157, 171, addPrefix(ResponseDataImporter.ADAT, ResponseDataImporter.RMID_PREFIX)),
                      new FixedLenFieldDescription(172, 179, ResponseDataImporter.RMID_IDAB));

        addDefinition(DialogEndMessageWorker.DIALOG_TABLENAME,
                      new FixedLenFieldDescription(73, 92, DialogEndMessageWorker.DIALOG_DIRECT_ANZAHL),
                      new FixedLenFieldDescription(93, 116, DialogEndMessageWorker.DIALOG_DIRECT_DATUM));
    }

    /**
     * Fügt der statischen Map {@link #RECORD_TYPE_DEFINITIONS} die übergebene Definition hinzu
     *
     * @param dialogTableName
     * @param fixedLenFieldDescriptions
     */
    private static void addDefinition(String dialogTableName, FixedLenFieldDescription... fixedLenFieldDescriptions) {
        // Erst die Header-Attribute hinzufügen
        FixedLenFieldDescription[] description = Stream.concat(Arrays.stream(fixedLenFieldDescriptions), Arrays.stream(HEADER_ATTRIBUTES))
                .toArray(size -> (FixedLenFieldDescription[])Array.newInstance(fixedLenFieldDescriptions.getClass().getComponentType(), size));
        // Definition inkl. Header-Attribute hinzufügen
        RECORD_TYPE_DEFINITIONS.put(dialogTableName, new FixedLenRecordType[]{ new FixedLenRecordType("", description) });
    }

    /**
     * Fügt einem Flednamen einen Prefix hinzu
     *
     * @param fieldNameWithoutPrefix
     * @param prefix
     * @return
     */
    static String addPrefix(String fieldNameWithoutPrefix, String prefix) {
        return prefix + "_" + fieldNameWithoutPrefix;
    }


    public static FixedLenRecordType[] getFixedLengthDefinition(String dialogType, String payloadLength) {
        // Check, ob der Typ angepasst werden muss
        String type = iPartsTextToDIALOGDataHelper.checkImportMessageType(dialogType, payloadLength);
        return RECORD_TYPE_DEFINITIONS.get(type);
    }

    public static void initImporterMap(EtkProject project) {
        IMPORTER_MAP.put(CodeMasterDataImporter.DIALOG_TABLENAME, new DialogDirectImportFunction("CodeImporter (RES)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new CodeMasterDataImporter(project);
            }
        });
        IMPORTER_MAP.put(ColorTableImporter.DIALOG_TABLENAME, new DialogDirectImportFunction("ColorTableImporter (FTS)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new ColorTableImporter(project);
            }
        });
        IMPORTER_MAP.put(MasterDataDialogSeriesImporter.DIALOG_TABLENAME, new DialogDirectImportFunction("MasterDataDialogSeriesImporter (BRS)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new MasterDataDialogSeriesImporter(project);
            }
        });
        IMPORTER_MAP.put(MasterDataDialogModelImporter.DIALOG_TABLENAME, new DialogDirectImportFunction("MasterDataDialogModelImporter (BMS)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new MasterDataDialogModelImporter(project);
            }
        });
        IMPORTER_MAP.put(ColorNumberImporter.DIALOG_TABLENAME, new DialogDirectImportFunction("ColorNumberImporter (FNR)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new ColorNumberImporter(project);
            }
        });
        IMPORTER_MAP.put(MasterDataDialogImporter.DIALOG_TABLENAME_TS1, new DialogDirectImportFunction("MasterDataDialogImporter (TS1)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new MasterDataDialogImporter(project);
            }
        });
        IMPORTER_MAP.put(MasterDataDialogImporter.DIALOG_TABLENAME_TS2, new DialogDirectImportFunction("MasterDataDialogImporter (TS2)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new MasterDataDialogImporter(project);
            }
        });
        IMPORTER_MAP.put(MasterDataDialogImporter.DIALOG_TABLENAME_TS6, new DialogDirectImportFunction("MasterDataDialogImporter (TS6)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new MasterDataDialogImporter(project);
            }
        });
        IMPORTER_MAP.put(MasterDataDialogImporter.DIALOG_TABLENAME_GEWS, new DialogDirectImportFunction("MasterDataDialogImporter (GEWS)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new MasterDataDialogImporter(project);
            }
        });
        IMPORTER_MAP.put(DSRDataImporter.DIALOG_TABLENAME, new DialogDirectImportFunction("DSRDataImporter (TMK)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new DSRDataImporter(project);
            }
        });
        IMPORTER_MAP.put(HmMSmStructureImporter.DIALOG_TABLENAME, new DialogDirectImportFunction("HmMSmStructureImporter (KGVZ)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new HmMSmStructureImporter(project);
            }
        });
        IMPORTER_MAP.put(PartListDataImporter.BRTE_PREFIX, new DialogDirectImportFunction("PartListDataImporter (BRTE)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new PartListDataImporter(project, PartListDataImporter.IMPORT_TABLENAME_BRTE);
            }
        });
        IMPORTER_MAP.put(PartListTextDataImporter.DIALOG_TABLENAME, new DialogDirectImportFunction("PartListTextDataImporter (BCTX)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new PartListTextDataImporter(project);
            }
        });
        IMPORTER_MAP.put(GenericPartImporter.DIALOG_TABLENAME, new DialogDirectImportFunction("GenericPartImporter (BCTG)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new GenericPartImporter(project);
            }
        });
        IMPORTER_MAP.put(DIALOGPosTextImporter.DIALOG_TABLENAME, new DialogDirectImportFunction("DIALOGPosTextImporter (POSX)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new DIALOGPosTextImporter(project);
            }
        });
        IMPORTER_MAP.put(GenericInstallLocationImporter.DIALOG_TABLENAME, new DialogDirectImportFunction("GenericInstallLocationImporter (POS)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new GenericInstallLocationImporter(project);
            }
        });
        IMPORTER_MAP.put(FactoryDataImporter.WBRT_PREFIX, new DialogDirectImportFunction("FactoryDataImporter (WBRT)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new FactoryDataImporter(project, FactoryDataImporter.IMPORT_TABLENAME_WBRT);
            }
        });
        IMPORTER_MAP.put(iPartsFactoryDataTypes.COLORTABLE_PART.getDatasetValue(), new DialogDirectImportFunction("ColorTableFactoryDataImporter (WX10)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new ColorTableFactoryDataImporter(project, ColorTableFactoryDataImporter.TABLENAME_WX10);
            }
        });
        IMPORTER_MAP.put(ColorTableFactoryDataImporter.WY9_PREFIX, new DialogDirectImportFunction("ColorTableFactoryDataImporter (WY9)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new ColorTableFactoryDataImporter(project, ColorTableFactoryDataImporter.TABLENAME_WY9);
            }
        });
        IMPORTER_MAP.put(MasterDataDialogModelSeriesImporter.DIALOG_TABLENAME, new DialogDirectImportFunction("MasterDataDialogModelSeriesImporter (X2E)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new MasterDataDialogModelSeriesImporter(project);
            }
        });
        IMPORTER_MAP.put(SeriesCodesImporter.Y4E_PREFIX, new DialogDirectImportFunction("SeriesCodesImporter (Y4E)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new SeriesCodesImporter(project, SeriesCodesImporter.IMPORT_TABLENAME_Y4E);
            }
        });
        IMPORTER_MAP.put(EventDataImporter.DIALOG_TABLENAME, new DialogDirectImportFunction("EventDataImporter (EREI)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new EventDataImporter(project);
            }
        });
        IMPORTER_MAP.put(KemMasterDataImporter.DIALOG_TABLENAME, new DialogDirectImportFunction("KemMasterDataImporter (KES)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new KemMasterDataImporter(project);
            }
        });
        IMPORTER_MAP.put(ZBVEDataImporter.DIALOG_TABLENAME, new DialogDirectImportFunction("ZBVEDataImporter (ZBVE)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new ZBVEDataImporter(project);
            }
        });
        IMPORTER_MAP.put(ColorTablePartOrContentImporter.Y9E_PREFIX, new DialogDirectImportFunction("ColorTablePartOrContentImporter (Y9E)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new ColorTablePartOrContentImporter(project, ColorTablePartOrContentImporter.IMPORT_TABLENAME_Y9E);
            }
        });
        IMPORTER_MAP.put(ColorTablePartOrContentImporter.X10E_PREFIX, new DialogDirectImportFunction("ColorTablePartOrContentImporter (X10E)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new ColorTablePartOrContentImporter(project, ColorTablePartOrContentImporter.IMPORT_TABLENAME_X10E);
            }
        });
        IMPORTER_MAP.put(ReplacementPartsConstImporter.DIALOG_TABLENAME, new DialogDirectImportFunction("ReplacementPartsConstImporter (TS7)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new ReplacementPartsConstImporter(project);
            }
        });
        IMPORTER_MAP.put(VTNVDataImporter.DIALOG_TABLENAME, new DialogDirectImportFunction("VTNVDataImporter (VTNV)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new VTNVDataImporter(project);
            }
        });
        IMPORTER_MAP.put(FootNoteMatRefImporter.DIALOG_TABLENAME, new DialogDirectImportFunction("FootNoteMatRefImporter (VTFN)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new FootNoteMatRefImporter(project);
            }
        });
        IMPORTER_MAP.put(FootNotePosRefImporter.DIALOG_TABLENAME, new DialogDirectImportFunction("FootNotePosRefImporter (VBFN)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new FootNotePosRefImporter(project);
            }
        });
        IMPORTER_MAP.put(VFNDataImporter.DIALOG_TABLENAME, new DialogDirectImportFunction("VFNDataImporter (VFN)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new VFNDataImporter(project);
            }
        });
        IMPORTER_MAP.put(iPartsFactoryDataTypes.COLORTABLE_CONTENT_AS.getDatasetValue(), new DialogDirectImportFunction("ColorTableFactoryDataImporter (VX9)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new ColorTableFactoryDataImporter(project, ColorTableFactoryDataImporter.TABLENAME_VX9);
            }
        });
        IMPORTER_MAP.put(iPartsFactoryDataTypes.COLORTABLE_PART_AS.getDatasetValue(), new DialogDirectImportFunction("ColorTableFactoryDataImporter (VX10)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new ColorTableFactoryDataImporter(project, ColorTableFactoryDataImporter.TABLENAME_VX10);
            }
        });
        IMPORTER_MAP.put(FactoryDataImporter.VBW_PREFIX, new DialogDirectImportFunction("FactoryDataImporter (VBW)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new FactoryDataImporter(project, FactoryDataImporter.IMPORT_TABLENAME_VBW);
            }
        });
        IMPORTER_MAP.put(SCTVDataImporter.DIALOG_TABLENAME, new DialogDirectImportFunction("SCTVDataImporter (SCTV)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new SCTVDataImporter(project);
            }
        });
        IMPORTER_MAP.put(PartListAddDataImporter.VBRT_PREFIX, new DialogDirectImportFunction("PartListAddDataImporter (VBRT)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new PartListAddDataImporter(project, PartListAddDataImporter.IMPORT_TABLENAME_VBRT);
            }
        });
        IMPORTER_MAP.put(MasterDataDialogImporter.DIALOG_TABLENAME_VTNR, new DialogDirectImportFunction("MasterDataDialogImporter (VTNR)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new MasterDataDialogImporter(project);
            }
        });
        IMPORTER_MAP.put(ResponseDataImporter.RMDA_PREFIX, new DialogDirectImportFunction("ResponseDataImporter (RMDA)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new ResponseDataImporter(project, ResponseDataImporter.TABLENAME_RMDA);
            }
        });
        IMPORTER_MAP.put(ResponseDataImporter.RMID_PREFIX, new DialogDirectImportFunction("ResponseDataImporter (RMID)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new ResponseDataImporter(project, ResponseDataImporter.TABLENAME_RMID);
            }
        });
        IMPORTER_MAP.put(DialogEndMessageWorker.DIALOG_TABLENAME, new DialogDirectImportFunction("DialogEndMessageWorker (ENDE)") {
            @Override
            public AbstractDataImporter createImporter() {
                return new DialogEndMessageWorker(project);
            }
        });
    }

    public static DialogDirectImportFunction getImporterFunction(String dialogType) {
        return IMPORTER_MAP.get(dialogType);
    }
}
