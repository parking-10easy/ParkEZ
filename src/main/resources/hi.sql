INSERT INTO parking_zone (
    id,
    parking_lot_id,
    name,
    image_url,
    status,
    review_count,
    created_at
) VALUES (
             3,
             2,
             '테스트 존 A',
             'https://example.com/image.png',
             'AVAILABLE',
             0,
             NOW()
         );