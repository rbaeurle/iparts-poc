-- Performance-Test: Import vom RSK Lexikon

-- Der Owner "ipartsora" muss ggf. an die eigene DB angepasst werden 

-- Nötig, damit Ampersands in String Konstanten funktionieren (Ausführung in Oracle SQL Developer schlägt sonst fehl)
set define off

-- Vorbereitungen
-- Dummy-Datensätze von einem RSK-Eintrag für After-Sales erzeugen, damit darauf später ein Update gemacht werden kann
insert into ipartsora.da_dict_meta ( da_dict_meta_txtkind_id, da_dict_meta_textid, da_dict_meta_foreignid, da_dict_meta_source, da_dict_meta_state, da_dict_meta_create, da_dict_meta_change, da_dict_meta_userid, t_stamp )  values('1028F8F6CEA646D78AE13DD8BFFCDC3D', 'RSK.12345abcd', '12345abcd', 'RSK', 'RELEASED', '20160405090104', ' ', 'rsk01', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.12345abcd', 'BG', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.12345abcd', 'CS', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.12345abcd', 'DA', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.12345abcd', 'DE', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.12345abcd', 'EL', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.12345abcd', 'EN', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.12345abcd', 'ES', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.12345abcd', 'FI', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.12345abcd', 'FR', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.12345abcd', 'HU', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.12345abcd', 'IT', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.12345abcd', 'JA', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.12345abcd', 'NL', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.12345abcd', 'PO', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.12345abcd', 'PL', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.12345abcd', 'PT', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.12345abcd', 'RO', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.12345abcd', 'RU', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.12345abcd', 'SK', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.12345abcd', 'SV', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.12345abcd', 'TR', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.12345abcd', 'ZH', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'BG', 'RSK.12345abcd', 'CHASSIS Test', 'RSK.12345abcd', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'CS', 'RSK.12345abcd', 'CHASSIS Test', 'RSK.12345abcd', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'DA', 'RSK.12345abcd', 'CHASSIS Test', 'RSK.12345abcd', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'DE', 'RSK.12345abcd', 'CHASSIS Test', 'RSK.12345abcd', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'EL', 'RSK.12345abcd', 'CHASSIS Test', 'RSK.12345abcd', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'EN', 'RSK.12345abcd', 'CHASSIS Test', 'RSK.12345abcd', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'ES', 'RSK.12345abcd', 'CHASSIS Test', 'RSK.12345abcd', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'FI', 'RSK.12345abcd', 'CHASSIS Test', 'RSK.12345abcd', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'FR', 'RSK.12345abcd', 'CHASSIS Test', 'RSK.12345abcd', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'HU', 'RSK.12345abcd', 'CHASSIS Test', 'RSK.12345abcd', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'IT', 'RSK.12345abcd', 'CHASSIS Test', 'RSK.12345abcd', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'JA', 'RSK.12345abcd', 'CHASSIS Test', 'RSK.12345abcd', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'NL', 'RSK.12345abcd', 'CHASSIS Test', 'RSK.12345abcd', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'NO', 'RSK.12345abcd', 'CHASSIS Test', 'RSK.12345abcd', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'PL', 'RSK.12345abcd', 'CHASSIS Test', 'RSK.12345abcd', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'PT', 'RSK.12345abcd', 'CHASSIS Test', 'RSK.12345abcd', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'RO', 'RSK.12345abcd', 'CHASSIS Test', 'RSK.12345abcd', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'RU', 'RSK.12345abcd', 'CHASSIS Test', 'RSK.12345abcd', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'SK', 'RSK.12345abcd', 'CHASSIS Test', 'RSK.12345abcd', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'SV', 'RSK.12345abcd', 'CHASSIS Test', 'RSK.12345abcd', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'TR', 'RSK.12345abcd', 'CHASSIS Test', 'RSK.12345abcd', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'ZH', 'RSK.12345abcd', 'CHASSIS Test', 'RSK.12345abcd', '50556030');


-- PerformanceTest_START
-- Einen RSK-Eintrag für After-Sales hinzufügen
-- DBBatchStatement_START
select s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp from ipartsora.sprache where s_textid like 'RSKCD.%' ESCAPE '\' order by s_textid;
select da_dict_meta_txtkind_id, da_dict_meta_textid, da_dict_meta_foreignid, da_dict_meta_source, da_dict_meta_state, da_dict_meta_create, da_dict_meta_change, da_dict_meta_userid, t_stamp from ipartsora.da_dict_meta where da_dict_meta_txtkind_id = '1028F8F6CEA646D78AE13DD8BFFCDC3D' and da_dict_meta_textid = 'RSK.6789efghi';
select da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp from ipartsora.da_dict_sprache where da_dict_sprache_textid = 'RSK.6789efghi';
select da_dict_meta_txtkind_id, da_dict_meta_textid, da_dict_meta_foreignid, da_dict_meta_source, da_dict_meta_state, da_dict_meta_create, da_dict_meta_change, da_dict_meta_userid, t_stamp from ipartsora.da_dict_meta where da_dict_meta_txtkind_id = '1028F8F6CEA646D78AE13DD8BFFCDC3D' and da_dict_meta_textid = 'RSK.6789efghi';
insert into ipartsora.da_dict_meta ( da_dict_meta_txtkind_id, da_dict_meta_textid, da_dict_meta_foreignid, da_dict_meta_source, da_dict_meta_state, da_dict_meta_create, da_dict_meta_change, da_dict_meta_userid, t_stamp )  values('1028F8F6CEA646D78AE13DD8BFFCDC3D', 'RSK.6789efghi', '6789efghi', 'RSK', 'RELEASED', '20160405090104', ' ', 'rsk01', '50556030');
-- DBBatchStatement_END
-- DBBatchStatement_START
select da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp from ipartsora.da_dict_sprache where da_dict_sprache_textid = 'RSK.6789efghi' and da_dict_sprache_sprach = 'DA';
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.6789efghi', 'BG', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.6789efghi', 'CS', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.6789efghi', 'DA', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.6789efghi', 'DE', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.6789efghi', 'EL', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.6789efghi', 'EN', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.6789efghi', 'ES', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.6789efghi', 'FI', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.6789efghi', 'FR', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.6789efghi', 'HU', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.6789efghi', 'IT', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.6789efghi', 'JA', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.6789efghi', 'NL', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.6789efghi', 'PO', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.6789efghi', 'PL', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.6789efghi', 'PT', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.6789efghi', 'RO', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.6789efghi', 'RU', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.6789efghi', 'SK', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.6789efghi', 'SV', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.6789efghi', 'TR', '20160405090104', ' ', 'RELEASED', '50556030');
insert into ipartsora.da_dict_sprache ( da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp )  values('RSK.6789efghi', 'ZH', '20160405090104', ' ', 'RELEASED', '50556030');
-- DBBatchStatement_END
-- DBBatchStatement_START
select da_dict_tku_txtkind_id, da_dict_tku_feld, da_dict_tku_change, da_dict_tku_userid, t_stamp from ipartsora.da_dict_txtkind_usage where da_dict_tku_txtkind_id = '1028F8F6CEA646D78AE13DD8BFFCDC3D';
select s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp from ipartsora.sprache where s_textid = 'RSK.6789efghi';
select s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp from ipartsora.sprache where s_textid = 'RSK.6789efghi';
select s_feld from ipartsora.sprache where s_feld = 'MAT.M_TEXTNR' and s_textnr = 'RSK.6789efghi';
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'BG', 'RSK.6789efghi', 'ENGINE Test', 'RSK.6789efghi', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'CS', 'RSK.6789efghi', 'ENGINE Test', 'RSK.6789efghi', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'DA', 'RSK.6789efghi', 'ENGINE Test', 'RSK.6789efghi', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'DE', 'RSK.6789efghi', 'ENGINE Test', 'RSK.6789efghi', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'EL', 'RSK.6789efghi', 'ENGINE Test', 'RSK.6789efghi', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'EN', 'RSK.6789efghi', 'ENGINE Test', 'RSK.6789efghi', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'ES', 'RSK.6789efghi', 'ENGINE Test', 'RSK.6789efghi', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'FI', 'RSK.6789efghi', 'ENGINE Test', 'RSK.6789efghi', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'FR', 'RSK.6789efghi', 'ENGINE Test', 'RSK.6789efghi', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'HU', 'RSK.6789efghi', 'ENGINE Test', 'RSK.6789efghi', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'IT', 'RSK.6789efghi', 'ENGINE Test', 'RSK.6789efghi', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'JA', 'RSK.6789efghi', 'ENGINE Test', 'RSK.6789efghi', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'NL', 'RSK.6789efghi', 'ENGINE Test', 'RSK.6789efghi', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'NO', 'RSK.6789efghi', 'ENGINE Test', 'RSK.6789efghi', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'PL', 'RSK.6789efghi', 'ENGINE Test', 'RSK.6789efghi', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'PT', 'RSK.6789efghi', 'ENGINE Test', 'RSK.6789efghi', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'RO', 'RSK.6789efghi', 'ENGINE Test', 'RSK.6789efghi', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'RU', 'RSK.6789efghi', 'ENGINE Test', 'RSK.6789efghi', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'SK', 'RSK.6789efghi', 'ENGINE Test', 'RSK.6789efghi', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'SV', 'RSK.6789efghi', 'ENGINE Test', 'RSK.6789efghi', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'TR', 'RSK.6789efghi', 'ENGINE Test', 'RSK.6789efghi', '50556030');
insert into ipartsora.sprache ( s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp )  values('MAT.M_TEXTNR', 'ZH', 'RSK.6789efghi', 'ENGINE Test', 'RSK.6789efghi', '50556030');
-- DBBatchStatement_END
-- DBBatchStatement_START
select s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp from ipartsora.sprache where s_textid = 'RSK.6789efghi';

-- Einen RSK-Eintrag für After-Sales ändern
select s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp from ipartsora.sprache where s_textid = 'RSK.12345abcd';
select da_dict_sprache_textid, da_dict_sprache_sprach, da_dict_sprache_create, da_dict_sprache_change, da_dict_sprache_status, t_stamp from ipartsora.da_dict_sprache where da_dict_sprache_textid = 'RSK.12345abcd';
update ipartsora.da_dict_meta set da_dict_meta_txtkind_id='1028F8F6CEA646D78AE13DD8BFFCDC3D', da_dict_meta_textid='RSK.12345abcd', da_dict_meta_foreignid='12345abcd', da_dict_meta_source='RSK', da_dict_meta_state='RELEASED', da_dict_meta_create='20160301172050', da_dict_meta_change='20160405090105', da_dict_meta_userid='rsk01', t_stamp='50556031' where da_dict_meta_txtkind_id = '1028F8F6CEA646D78AE13DD8BFFCDC3D' and da_dict_meta_textid = 'RSK.12345abcd';
-- DBBatchStatement_END
-- DBBatchStatement_START
select s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp from ipartsora.sprache where s_textid = 'RSK.12345abcd';
select s_feld, s_textnr, s_sprach, s_benenn, s_textid from ipartsora.sprache where s_feld = 'MAT.M_TEXTNR' and s_textnr = 'RSK.12345abcd';
update ipartsora.sprache set s_feld='MAT.M_TEXTNR', s_sprach='BG', s_textnr='RSK.12345abcd', s_benenn='CHASSIS neu', s_textid='RSK.12345abcd', t_stamp='50556031' where s_feld = 'MAT.M_TEXTNR' and s_textnr = 'RSK.12345abcd' and s_sprach = 'BG';
update ipartsora.sprache set s_feld='MAT.M_TEXTNR', s_sprach='CS', s_textnr='RSK.12345abcd', s_benenn='CHASSIS neu', s_textid='RSK.12345abcd', t_stamp='50556031' where s_feld = 'MAT.M_TEXTNR' and s_textnr = 'RSK.12345abcd' and s_sprach = 'CS';
update ipartsora.sprache set s_feld='MAT.M_TEXTNR', s_sprach='DA', s_textnr='RSK.12345abcd', s_benenn='CHASSIS neu', s_textid='RSK.12345abcd', t_stamp='50556031' where s_feld = 'MAT.M_TEXTNR' and s_textnr = 'RSK.12345abcd' and s_sprach = 'DA';
update ipartsora.sprache set s_feld='MAT.M_TEXTNR', s_sprach='DE', s_textnr='RSK.12345abcd', s_benenn='CHASSIS neu', s_textid='RSK.12345abcd', t_stamp='50556031' where s_feld = 'MAT.M_TEXTNR' and s_textnr = 'RSK.12345abcd' and s_sprach = 'DE';
update ipartsora.sprache set s_feld='MAT.M_TEXTNR', s_sprach='EL', s_textnr='RSK.12345abcd', s_benenn='CHASSIS neu', s_textid='RSK.12345abcd', t_stamp='50556031' where s_feld = 'MAT.M_TEXTNR' and s_textnr = 'RSK.12345abcd' and s_sprach = 'EL';
update ipartsora.sprache set s_feld='MAT.M_TEXTNR', s_sprach='EN', s_textnr='RSK.12345abcd', s_benenn='CHASSIS neu', s_textid='RSK.12345abcd', t_stamp='50556031' where s_feld = 'MAT.M_TEXTNR' and s_textnr = 'RSK.12345abcd' and s_sprach = 'EN';
update ipartsora.sprache set s_feld='MAT.M_TEXTNR', s_sprach='ES', s_textnr='RSK.12345abcd', s_benenn='CHASSIS neu', s_textid='RSK.12345abcd', t_stamp='50556031' where s_feld = 'MAT.M_TEXTNR' and s_textnr = 'RSK.12345abcd' and s_sprach = 'ES';
update ipartsora.sprache set s_feld='MAT.M_TEXTNR', s_sprach='FI', s_textnr='RSK.12345abcd', s_benenn='CHASSIS neu', s_textid='RSK.12345abcd', t_stamp='50556031' where s_feld = 'MAT.M_TEXTNR' and s_textnr = 'RSK.12345abcd' and s_sprach = 'FI';
update ipartsora.sprache set s_feld='MAT.M_TEXTNR', s_sprach='FR', s_textnr='RSK.12345abcd', s_benenn='CHASSIS neu', s_textid='RSK.12345abcd', t_stamp='50556031' where s_feld = 'MAT.M_TEXTNR' and s_textnr = 'RSK.12345abcd' and s_sprach = 'FR';
update ipartsora.sprache set s_feld='MAT.M_TEXTNR', s_sprach='HU', s_textnr='RSK.12345abcd', s_benenn='CHASSIS neu', s_textid='RSK.12345abcd', t_stamp='50556031' where s_feld = 'MAT.M_TEXTNR' and s_textnr = 'RSK.12345abcd' and s_sprach = 'HU';
update ipartsora.sprache set s_feld='MAT.M_TEXTNR', s_sprach='IT', s_textnr='RSK.12345abcd', s_benenn='CHASSIS neu', s_textid='RSK.12345abcd', t_stamp='50556031' where s_feld = 'MAT.M_TEXTNR' and s_textnr = 'RSK.12345abcd' and s_sprach = 'IT';
update ipartsora.sprache set s_feld='MAT.M_TEXTNR', s_sprach='JA', s_textnr='RSK.12345abcd', s_benenn='CHASSIS neu', s_textid='RSK.12345abcd', t_stamp='50556031' where s_feld = 'MAT.M_TEXTNR' and s_textnr = 'RSK.12345abcd' and s_sprach = 'JA';
update ipartsora.sprache set s_feld='MAT.M_TEXTNR', s_sprach='NL', s_textnr='RSK.12345abcd', s_benenn='CHASSIS neu', s_textid='RSK.12345abcd', t_stamp='50556031' where s_feld = 'MAT.M_TEXTNR' and s_textnr = 'RSK.12345abcd' and s_sprach = 'NL';
update ipartsora.sprache set s_feld='MAT.M_TEXTNR', s_sprach='NO', s_textnr='RSK.12345abcd', s_benenn='CHASSIS neu', s_textid='RSK.12345abcd', t_stamp='50556031' where s_feld = 'MAT.M_TEXTNR' and s_textnr = 'RSK.12345abcd' and s_sprach = 'NO';
update ipartsora.sprache set s_feld='MAT.M_TEXTNR', s_sprach='PL', s_textnr='RSK.12345abcd', s_benenn='CHASSIS neu', s_textid='RSK.12345abcd', t_stamp='50556031' where s_feld = 'MAT.M_TEXTNR' and s_textnr = 'RSK.12345abcd' and s_sprach = 'PL';
update ipartsora.sprache set s_feld='MAT.M_TEXTNR', s_sprach='PT', s_textnr='RSK.12345abcd', s_benenn='CHASSIS neu', s_textid='RSK.12345abcd', t_stamp='50556031' where s_feld = 'MAT.M_TEXTNR' and s_textnr = 'RSK.12345abcd' and s_sprach = 'PT';
update ipartsora.sprache set s_feld='MAT.M_TEXTNR', s_sprach='RO', s_textnr='RSK.12345abcd', s_benenn='CHASSIS neu', s_textid='RSK.12345abcd', t_stamp='50556031' where s_feld = 'MAT.M_TEXTNR' and s_textnr = 'RSK.12345abcd' and s_sprach = 'RO';
update ipartsora.sprache set s_feld='MAT.M_TEXTNR', s_sprach='RU', s_textnr='RSK.12345abcd', s_benenn='CHASSIS neu', s_textid='RSK.12345abcd', t_stamp='50556031' where s_feld = 'MAT.M_TEXTNR' and s_textnr = 'RSK.12345abcd' and s_sprach = 'RU';
update ipartsora.sprache set s_feld='MAT.M_TEXTNR', s_sprach='SK', s_textnr='RSK.12345abcd', s_benenn='CHASSIS neu', s_textid='RSK.12345abcd', t_stamp='50556031' where s_feld = 'MAT.M_TEXTNR' and s_textnr = 'RSK.12345abcd' and s_sprach = 'SK';
update ipartsora.sprache set s_feld='MAT.M_TEXTNR', s_sprach='SV', s_textnr='RSK.12345abcd', s_benenn='CHASSIS neu', s_textid='RSK.12345abcd', t_stamp='50556031' where s_feld = 'MAT.M_TEXTNR' and s_textnr = 'RSK.12345abcd' and s_sprach = 'SV';
update ipartsora.sprache set s_feld='MAT.M_TEXTNR', s_sprach='TR', s_textnr='RSK.12345abcd', s_benenn='CHASSIS neu', s_textid='RSK.12345abcd', t_stamp='50556031' where s_feld = 'MAT.M_TEXTNR' and s_textnr = 'RSK.12345abcd' and s_sprach = 'TR';
update ipartsora.sprache set s_feld='MAT.M_TEXTNR', s_sprach='ZH', s_textnr='RSK.12345abcd', s_benenn='CHASSIS neu', s_textid='RSK.12345abcd', t_stamp='50556031' where s_feld = 'MAT.M_TEXTNR' and s_textnr = 'RSK.12345abcd' and s_sprach = 'ZH';
-- DBBatchStatement_END
select s_feld, s_sprach, s_textnr, s_benenn, s_textid, t_stamp from ipartsora.sprache where s_textid = 'RSK.12345abcd';
-- PerformanceTest_END


-- Löschen der neu hinzugefügten Daten (nicht Teil des Performance-Tests)
delete from ipartsora.da_dict_meta where da_dict_meta_txtkind_id = '1028F8F6CEA646D78AE13DD8BFFCDC3D' and da_dict_meta_textid = 'RSK.12345abcd';
delete from ipartsora.da_dict_sprache where da_dict_sprache_textid = 'RSK.12345abcd';
delete from ipartsora.sprache where s_feld = 'MAT.M_TEXTNR' and s_textnr = 'RSK.12345abcd';

delete from ipartsora.da_dict_meta where da_dict_meta_txtkind_id = '1028F8F6CEA646D78AE13DD8BFFCDC3D' and da_dict_meta_textid = 'RSK.6789efghi';
delete from ipartsora.da_dict_sprache where da_dict_sprache_textid = 'RSK.6789efghi';
delete from ipartsora.sprache where s_feld = 'MAT.M_TEXTNR' and s_textnr = 'RSK.6789efghi';