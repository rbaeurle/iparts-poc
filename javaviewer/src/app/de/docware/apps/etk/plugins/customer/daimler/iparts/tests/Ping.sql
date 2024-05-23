-- Performance-Test: Simpler Ping durch ein Select-Statement ohne Ergebnis

-- PerformanceTest_START
select * from ipartsora.estruct where es_key = 'Ping';
-- PerformanceTest_END