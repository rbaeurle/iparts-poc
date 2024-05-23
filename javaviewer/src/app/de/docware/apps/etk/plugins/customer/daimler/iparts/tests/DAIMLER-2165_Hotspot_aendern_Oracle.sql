-- Performance-Test: Hotspot ändern und Modul speichern

-- Der Owner "ipartsora" muss ggf. an die eigene DB angepasst werden 

-- Nötig, damit Ampersands in String Konstanten funktionieren (Ausführung in Oracle SQL Developer schlägt sonst fehl)
set define off

-- Vorbereitungen
-- Dummy-Stücklisteneintrag in der fiktiven Stückliste "__TestModule_DAIMLER-2196" erzeugen
insert into ipartsora.katalog (k_pos, k_menge, k_mengeart, k_aa, k_codes, k_datefrom, k_dateto, k_hierarchy, k_seqnr, k_vari, k_ver, k_lfdnr, k_sach, k_sver, k_art, k_matnr, k_mver, k_ebene, k_bestflag, t_stamp)  values('210', '1', ' ', 'FW', 'M626;', '20140204065734', ' ', '02', '7', '__TestModule_DAIMLER-2196', ' ', '00007', ' ', ' ', ' ', 'A6269930096', ' ', ' ', '0', '50549BB2');

-- PerformanceTest_START
-- DBBatchStatement_START
-- Ändern der Positionsnummer in einem Dummy-Stücklisteneintrag (Hotspot) und Modul speichern
update ipartsora.katalog set k_pos='200', k_menge='1', k_mengeart=' ', k_aa='FW', k_codes='M626;', k_datefrom='20140204065734', k_dateto=' ', k_hierarchy='02', k_seqnr='7', k_vari='__TestModule_DAIMLER-2196', k_ver=' ', k_lfdnr='00007', k_sach=' ', k_sver=' ', k_art=' ', k_matnr='A6269930096', k_mver=' ', k_ebene=' ', k_bestflag='0', t_stamp='50549BB2' where k_vari = '__TestModule_DAIMLER-2196' and k_ver = ' ' and k_lfdnr = '00007';
-- DBBatchStatement_END
select ppa_order_guid, ppa_vari, ppa_ver, ppa_lfdnr, ppa_src_key, ppa_pos, ppa_sach, ppa_zgs, ppa_reldate, ppa_context, ppa_sent, t_stamp from ipartsora.da_picorder_parts where ppa_vari = '__TestModule_DAIMLER-2196' and ppa_ver = '';
-- PerformanceTest_END

-- Dummy-Stücklisteneintrag wieder löschen (nicht Teil des Performance-Tests)
delete from ipartsora.katalog where k_vari = '__TestModule_DAIMLER-2196' and k_ver = ' ' and k_lfdnr = '00007';