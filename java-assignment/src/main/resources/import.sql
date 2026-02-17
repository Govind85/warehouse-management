INSERT INTO store(id, name, quantityProductsInStock) VALUES (1, 'TONSTAD', 10);
INSERT INTO store(id, name, quantityProductsInStock) VALUES (2, 'KALLAX', 5);
INSERT INTO store(id, name, quantityProductsInStock) VALUES (3, 'BESTÅ', 3);
ALTER SEQUENCE store_seq RESTART WITH 4;

INSERT INTO product(id, name, stock) VALUES (1, 'TONSTAD', 10);
INSERT INTO product(id, name, stock) VALUES (2, 'KALLAX', 5);
INSERT INTO product(id, name, stock) VALUES (3, 'BESTÅ', 3);
INSERT INTO product(id, name, stock) VALUES (4, 'HEMNES', 8);
INSERT INTO product(id, name, stock) VALUES (5, 'MALM', 12);
INSERT INTO product(id, name, stock) VALUES (6, 'BILLY', 15);
ALTER SEQUENCE product_seq RESTART WITH 7;

INSERT INTO warehouse(id, businessUnitCode, location, capacity, stock, createdAt, archivedAt) 
VALUES (1, 'MWH.001', 'ZWOLLE-001', 100, 10, '2024-07-01', null);
INSERT INTO warehouse(id, businessUnitCode, location, capacity, stock, createdAt, archivedAt)
VALUES (2, 'MWH.012', 'AMSTERDAM-001', 50, 5, '2023-07-01', null);
INSERT INTO warehouse(id, businessUnitCode, location, capacity, stock, createdAt, archivedAt)
VALUES (3, 'MWH.023', 'TILBURG-001', 30, 27, '2021-02-01', null);
INSERT INTO warehouse(id, businessUnitCode, location, capacity, stock, createdAt, archivedAt)
VALUES (4, 'MWH.024', 'TILBURG-002', 30, 27, '2021-02-01', '2021-02-02');
ALTER SEQUENCE warehouse_seq RESTART WITH 5;
