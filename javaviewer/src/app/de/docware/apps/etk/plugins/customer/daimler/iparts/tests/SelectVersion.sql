-- Performance-Test: Versionsabfrage der Oracle DB

-- PerformanceTest_START
select version from product_component_version where product like 'Oracle Database%';
-- PerformanceTest_END