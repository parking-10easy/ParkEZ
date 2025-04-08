INSERT INTO users (email, password, nickname, phone, role)
VALUES ('owner@owner.com', 'password', '소유주', '111-1111-1111', 'ROLE_OWNER');

INSERT INTO parking_lot
(owner_id, name, address, opened_at, closed_at, price_per_hour, description, quantity, charge_type, source_type, status, created_at)
VALUES
    (1, '테스트 주차장', '서울시 강남구 테헤란로 1111', '08:00:00', '22:00:00', 2000, '테스트용 주차장입니다.', 100, 'PAID', 'OWNER_REGISTERED', 'OPEN', CURRENT_TIMESTAMP);

INSERT INTO parking_lot
(owner_id, name, address, opened_at, closed_at, price_per_hour, description, quantity, charge_type, source_type, status, created_at)
VALUES
    (1, '한빛 주차장', '부산시 사하구 테헤란로 222', '08:00:00', '22:00:00', 2000, '테스트용 주차장입니다.', 100, 'PAID', 'OWNER_REGISTERED', 'OPEN', CURRENT_TIMESTAMP);