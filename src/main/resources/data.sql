-- 카테고리 데이터
INSERT INTO category (category_name) VALUES 
('상의'),
('아우터'),
('하의'),
('원피스/치마'),
('신발'),
('소품/ACC');

-- 테스트 사용자 데이터 (개발용)
---- 비밀번호: "Password123!" (BCrypt 암호화됨) - 특수문자 포함
--INSERT INTO member (email, user_name, birth_date, gender, password, password_expired, phone_num, banned, withdraw, provider, role) VALUES
--('test@example.com', '테스트사용자', '1990-01-01', 'M', '$2a$10$e0MYzXyjpJS7Pd2AWFdkUOtqlAzCzM1DurdGBISaACfM5lZjBYYGS', false, '010-1234-5678', false, false, 'EMAIL', 'USER'),
--('admin@test.com', '관리자', '1985-05-15', 'F', '$2a$10$e0MYzXyjpJS7Pd2AWFdkUOtqlAzCzM1DurdGBISaACfM5lZjBYYGS', false, '010-9999-0000', false, false, 'EMAIL', 'ADMIN'),
--('user@test.com', '일반사용자', '1995-12-25', 'M', '$2a$10$e0MYzXyjpJS7Pd2AWFdkUOtqlAzCzM1DurdGBISaACfM5lZjBYYGS', false, '010-5555-1111', false, false, 'EMAIL', 'USER'),
--('sha0209@dgu.ac.kr', '현아', '2001-02-09', 'F', null, false, '010-5555-1111', false, false, 'GOOGLE', 'USER');
--
---- Profile 데이터 (Member와 1:1 관계)
---- 각 사용자의 프로필 정보 - 올바른 컬럼명 사용
--INSERT INTO profile (member_user_id, preferred_style, height, weight, shoe_size, profile_image_url, user_base_image_url, avatar_base_image_url) VALUES
---- 테스트사용자 (user_id: 1)
--(1, 'CASUAL', 175, 70, 270, 'https://example.com/profile/user1.jpg', 'https://example.com/base/user1_base.jpg', 'https://example.com/avatar/user1_avatar.jpg'),
---- 관리자 (user_id: 2)
--(2, 'FORMAL', 165, 55, 240, 'https://example.com/profile/admin.jpg', 'https://example.com/base/admin_base.jpg', 'https://example.com/avatar/admin_avatar.jpg'),
---- 일반사용자 (user_id: 3)
--(3, 'STREET', 180, 75, 280, 'https://example.com/profile/user3.jpg', 'https://example.com/base/user3_base.jpg', 'https://example.com/avatar/user3_avatar.jpg'),
---- 현아 (user_id: 4)
--(4, 'CHIC', 160, 50, 230, 'https://example.com/profile/hyuna.jpg', 'https://example.com/base/hyuna_base.jpg', 'https://example.com/avatar/hyuna_avatar.jpg');

-- 실제 제품 데이터 (상의 카테고리)
INSERT INTO product (category_id, product_name, img1, img2, img3, img4, img5, content, price, sale, brand, gender, deleted, wishlist_count) VALUES 
(1, '포우하트키 반팔티 피그먼트 먹검정', 'https://image.msscdn.net/thumbnails/images/goods_img/20250507/5092396/5092396_17467021804474_big.jpg?w=780', 'https://image.msscdn.net/thumbnails/images/prd_img/20250507/5092396/detail_5092396_17467022535011_big.jpg?w=1200', 'https://image.msscdn.net/thumbnails/images/goods_img/20250507/5092396/5092396_17467021804474_big.jpg?w=1200', 'https://image.msscdn.net/thumbnails/images/prd_img/20250507/5092396/detail_5092396_17467022427459_big.jpg?w=1200', null, '꼼파뇨 브랜드의 포우하트키 반팔티입니다.', 33800, 30000, '꼼파뇨', 'UNISEX', false, 0),

(1, 'STRANGE PUPPY TEE WHITE', 'https://image.msscdn.net/thumbnails/images/goods_img/20250312/4887825/4887825_17419183215151_big.jpg?w=780', 'https://image.msscdn.net/thumbnails/snap/images/2025/03/27/ef27727fa39d4755be324ac7b4630dda.jpg', 'https://image.msscdn.net/thumbnails/images/goods_img/20250312/4887825/4887825_17419183215151_big.jpg?w=1200', 'https://image.msscdn.net/thumbnails/images/prd_img/20250312/4887825/detail_4887825_17453031919019_big.jpg?w=1200', 'https://ikkorea.i.hhosting.kr/mahagrid/product/top/MG2FMMT506AWH.jpg', '마하그리드 브랜드의 스트레인지 퍼피 티셔츠입니다.', 31200, 0, '마하그리드', 'UNISEX', false, 0),

(1, '[2PACK] 뉴웨이브 크루즈 + 에잇티즈 반팔 티셔츠 2PACK', 'https://image.msscdn.net/thumbnails/images/goods_img/20230331/3196754/3196754_17156466306086_big.jpg?w=780', 'https://image.msscdn.net/thumbnails/images/prd_img/20230331/3196754/detail_3196754_17452849742489_big.jpg?w=1200', 'https://image.msscdn.net/thumbnails/images/goods_img/20230331/3196754/3196754_17156466306086_big.jpg?w=1200', 'https://image.msscdn.net/thumbnails/images/prd_img/20230331/3196754/detail_3196754_17452849697570_big.jpg?w=1200', 'https://efairplay.img2.kr/BRAND/FP/FP_intro2021_.jpg', '페플 브랜드의 2팩 티셔츠 세트입니다.', 35300, 0, '페플', 'UNISEX', false, 0),

(1, '모노그램 모티프 티셔츠 - 블랙', 'https://image.msscdn.net/thumbnails/images/goods_img/20230321/3164861/3164861_16812654215756_big.jpg?w=780', 'https://image.msscdn.net/thumbnails/images/prd_img/20230321/3164861/detail_3164861_16812654385633_big.jpg?w=1200', 'https://image.msscdn.net/thumbnails/images/goods_img/20230321/3164861/3164861_16812654215756_big.jpg?w=1200', 'https://image.msscdn.net/thumbnails/images/prd_img/20230321/3164861/detail_3164861_16812654246172_big.jpg?w=1200', 'https://image.msscdn.net/display/images/2024/07/16/4f1e0aa9ece3462098ec68e41b1b99ce.png', '버버리 브랜드의 모노그램 모티프 티셔츠입니다.', 450000, 400000, '버버리', 'UNISEX', false, 0),

(1, '어드벤쳐 트럭커 포켓 반팔티셔츠 화이트', 'https://image.msscdn.net/thumbnails/images/goods_img/20220329/2451591/2451591_1_big.jpg?w=780', 'https://image.msscdn.net/thumbnails/images/goods_img/20220329/2451591/2451591_1_big.jpg?w=1200', 'https://image.msscdn.net/thumbnails/images/prd_img/20220329/2451591/detail_2451591_1_big.jpg?w=1200', 'https://image.msscdn.net/thumbnails/images/prd_img/20220329/2451591/detail_2451591_2_big.jpg?w=1200', 'https://th3point.speedgabia.com/fluke/2022SS/FST/fst141-1-wh.jpg', '플루크 브랜드의 어드벤쳐 트럭커 포켓 반팔티셔츠입니다.', 17800, 0, '플루크', 'UNISEX', false, 0),

(1, '[가나디] 반팔 티셔츠(WHITE)', 'https://image.msscdn.net/thumbnails/images/goods_img/20250508/5093305/5093305_17470949517563_big.jpg?w=780', 'https://image.msscdn.net/thumbnails/images/goods_img/20250508/5093305/5093305_17470949517563_big.jpg?w=1200', 'https://image.msscdn.net/thumbnails/images/prd_img/20250508/5093305/detail_5093305_17470949517563_big.jpg?w=1200', 'https://image.msscdn.net/thumbnails/images/prd_img/20250508/5093305/detail_5093305_17467475883958_big.jpg?w=1200', 'https://image.msscdn.net/thumbnails/images/prd_img/2025050917060168244752450681db769a69d6.png?w=1200', '스파오 브랜드의 가나디 반팔 티셔츠입니다.', 25900, 0, '스파오', 'UNISEX', false, 0),

(1, '플라워 프린팅 피그먼트 반팔티셔츠 LIGHT NAVY', 'https://image.msscdn.net/thumbnails/images/goods_img/20240405/4031358/4031358_17126658458825_big.jpg?w=780', 'https://image.msscdn.net/thumbnails/snap/images/2025/04/03/7606f871957f413ea24fb105c676a36a.jpg', 'https://image.msscdn.net/thumbnails/images/goods_img/20240405/4031358/4031358_17126658458825_big.jpg?w=1200', 'https://image.msscdn.net/thumbnails/images/prd_img/20240405/4031358/detail_4031358_17145431928095_big.jpg?w=1200', 'https://th3point.speedgabia.com/MNP/2024SS/MST/notice-mst155.jpg', '미니멀프로젝트 브랜드의 플라워 프린팅 피그먼트 반팔티셔츠입니다.', 27800, 0, '미니멀프로젝트', 'UNISEX', false, 0),

(1, 'MANIFEST 래글런 배색 트랙 오버핏 반팔티', 'https://image.msscdn.net/thumbnails/images/goods_img/20250312/4885278/4885278_17441569547718_big.jpg?w=780', 'https://image.msscdn.net/thumbnails/images/prd_img/20250312/4885278/detail_4885278_17441569667305_big.jpg?w=1200', 'https://image.msscdn.net/thumbnails/images/goods_img/20250312/4885278/4885278_17441569547718_big.jpg?w=1200', 'https://image.msscdn.net/thumbnails/images/prd_img/20250312/4885278/detail_4885278_17417477589572_big.jpg?w=1200', 'https://bakken.speedgabia.com/alvin/25ss_track_tshirt_banner.jpg', '앨빈클로 브랜드의 MANIFEST 래글런 배색 트랙 오버핏 반팔티입니다.', 24900, 0, '앨빈클로', 'UNISEX', false, 0),

(1, 'CUPID VTG 티셔츠 화이트', 'https://image.msscdn.net/thumbnails/images/goods_img/20250321/4929073/4929073_17503967102423_big.jpg?w=780', 'https://image.msscdn.net/thumbnails/images/prd_img/20250321/4929073/detail_4929073_17425420446864_big.jpg?w=1200', 'https://image.msscdn.net/thumbnails/images/goods_img/20250321/4929073/4929073_17503967102423_big.jpg?w=1200', 'https://image.msscdn.net/thumbnails/images/prd_img/20250321/4929073/detail_4929073_17503967204881_big.jpg?w=1200', 'https://olivedes.s3.ap-northeast-2.amazonaws.com/yuthentic/eve/NOTICE.jpg', '유센틱 브랜드의 CUPID VTG 티셔츠입니다.', 39200, 0, '유센틱', 'UNISEX', false, 0),

(1, '빈티지 스트라이프 숏 슬리브 티셔츠', 'https://image.msscdn.net/thumbnails/images/goods_img/20240405/4030331/4030331_17502121977776_big.jpg?w=780', 'https://image.msscdn.net/thumbnails/mfile_s01/_shopstaff/staff_666690826c9f1.jpg', 'https://image.msscdn.net/thumbnails/images/goods_img/20240405/4030331/4030331_17502121977776_big.jpg?w=1200', 'https://image.msscdn.net/thumbnails/images/prd_img/20240405/4030331/detail_4030331_17146155654297_big.jpg?w=1200', 'https://image.msscdn.net/thumbnails/images/prd_img/20240502111031585430489236632f6178eee3.jpg?w=1200', '웬즈데이오아시스 브랜드의 빈티지 스트라이프 숏 슬리브 티셔츠입니다.', 35100, 29900, '웬즈데이오아시스', 'UNISEX', false, 0),

(1, 'MATIN SMALL LINE LOGO STITCH CROP TOP IN BLACK', 'https://image.msscdn.net/thumbnails/images/goods_img/20240311/3937377/3937377_17101244913392_big.jpg?w=780', 'https://image.msscdn.net/thumbnails/images/goods_img/20240311/3937377/3937377_17101244913392_big.jpg?w=1200', null, null, 'https://matinkim.speedgabia.com/matinkim/960x1280/MK2411TS015MBB_1.jpg', '마뗑킴 브랜드의 MATIN SMALL LINE LOGO STITCH CROP TOP입니다.', 58000, 0, '마뗑킴', 'UNISEX', false, 0),

(1, '폭스 헤드 패치 레귤러 반소매 티셔츠 - 잉크 블루', 'https://image.msscdn.net/thumbnails/images/goods_img/20240812/4320262/4320262_17247428287462_big.jpg?w=780', 'https://image.msscdn.net/thumbnails/images/prd_img/20240812/4320262/detail_4320262_17247428437251_big.jpg?w=1200', 'https://image.msscdn.net/thumbnails/images/goods_img/20240812/4320262/4320262_17247428287462_big.jpg?w=1200', 'https://image.msscdn.net/thumbnails/images/prd_img/20240812/4320262/detail_4320262_17247428353938_big.jpg?w=1200', 'https://image.msscdn.net/display/images/2024/07/16/4f1e0aa9ece3462098ec68e41b1b99ce.png', '메종 키츠네 브랜드의 폭스 헤드 패치 레귤러 반소매 티셔츠입니다.', 96990, 92680, '메종 키츠네', 'UNISEX', false, 0),

(1, '[한삐우 착용] 1+1 티 팩 - 블랙:화이트', 'https://image.msscdn.net/thumbnails/images/goods_img/20250324/4931677/4931677_17500626312844_big.jpg?w=780', 'https://image.msscdn.net/thumbnails/images/prd_img/20250324/4931677/detail_4931677_17435587405140_big.jpg?w=1200', 'https://image.msscdn.net/thumbnails/images/goods_img/20250324/4931677/4931677_17500626312844_big.jpg?w=1200', 'https://image.msscdn.net/thumbnails/images/prd_img/20250324/4931677/detail_4931677_17500626422589_big.jpg?w=1200', 'https://image.msscdn.net/display/images/2024/07/16/30206d6036e74006af67368a538086fe.png', '푸마 브랜드의 1+1 티 팩입니다.', 22990, 0, '푸마', 'UNISEX', false, 0),

(1, 'T 앤지 티셔츠 - 화이트', 'https://image.msscdn.net/thumbnails/images/goods_img/20250219/4806712/4806712_17404751984632_big.jpg?w=780', 'https://image.msscdn.net/thumbnails/images/prd_img/20250219/4806712/detail_4806712_17404752113459_big.jpg?w=1200', 'https://image.msscdn.net/thumbnails/images/goods_img/20250219/4806712/4806712_17404751984632_big.jpg?w=1200', 'https://image.msscdn.net/thumbnails/images/prd_img/20250219/4806712/detail_4806712_17404752049102_big.jpg?w=1200', 'https://image.msscdn.net/display/images/2024/07/16/4f1e0aa9ece3462098ec68e41b1b99ce.png', '디젤 브랜드의 T 앤지 티셔츠입니다.', 180000, 129000, '디젤', 'UNISEX', false, 0);

-- 제품 변형 데이터 (ProductVariant) - 실제 제품들에 대한 사이즈/색상 옵션
-- 각 제품별로 다양한 사이즈와 색상 옵션 추가
INSERT INTO product_variant (product_id, size, color, quantity) VALUES 
-- 포우하트키 반팔티 (product_id: 1)
(1, 'S', 'BLACK', 15),
(1, 'M', 'BLACK', 20),
(1, 'L', 'BLACK', 18),
(1, 'XL', 'BLACK', 12),

-- STRANGE PUPPY TEE (product_id: 2)
(2, 'S', 'WHITE', 10),
(2, 'M', 'WHITE', 25),
(2, 'L', 'WHITE', 20),
(2, 'XL', 'WHITE', 15),

-- 2PACK 티셔츠 (product_id: 3)
(3, 'S', 'MULTI', 8),
(3, 'M', 'MULTI', 12),
(3, 'L', 'MULTI', 10),
(3, 'XL', 'MULTI', 6),

-- 버버리 모노그램 티셔츠 (product_id: 4)
(4, 'S', 'BLACK', 5),
(4, 'M', 'BLACK', 8),
(4, 'L', 'BLACK', 6),
(4, 'XL', 'BLACK', 4),

-- 플루크 어드벤쳐 티셔츠 (product_id: 5)
(5, 'S', 'WHITE', 20),
(5, 'M', 'WHITE', 30),
(5, 'L', 'WHITE', 25),
(5, 'XL', 'WHITE', 18),

-- 스파오 가나디 티셔츠 (product_id: 6)
(6, 'S', 'WHITE', 25),
(6, 'M', 'WHITE', 35),
(6, 'L', 'WHITE', 30),
(6, 'XL', 'WHITE', 20),

-- 미니멀프로젝트 플라워 티셔츠 (product_id: 7)
(7, 'S', 'NAVY', 12),
(7, 'M', 'NAVY', 18),
(7, 'L', 'NAVY', 15),
(7, 'XL', 'NAVY', 10),

-- 앨빈클로 MANIFEST 티셔츠 (product_id: 8)
(8, 'S', 'MULTI', 15),
(8, 'M', 'MULTI', 22),
(8, 'L', 'MULTI', 18),
(8, 'XL', 'MULTI', 12),

-- 유센틱 CUPID 티셔츠 (product_id: 9)
(9, 'S', 'WHITE', 8),
(9, 'M', 'WHITE', 12),
(9, 'L', 'WHITE', 10),
(9, 'XL', 'WHITE', 6),

-- 웬즈데이오아시스 빈티지 스트라이프 (product_id: 10)
(10, 'S', 'STRIPE', 10),
(10, 'M', 'STRIPE', 15),
(10, 'L', 'STRIPE', 12),
(10, 'XL', 'STRIPE', 8),

-- 마뗑킴 크롭탑 (product_id: 11)
(11, 'S', 'BLACK', 6),
(11, 'M', 'BLACK', 10),
(11, 'L', 'BLACK', 8),

-- 메종 키츠네 폭스 헤드 (product_id: 12)
(12, 'S', 'BLUE', 4),
(12, 'M', 'BLUE', 6),
(12, 'L', 'BLUE', 5),
(12, 'XL', 'BLUE', 3),

-- 푸마 1+1 티팩 (product_id: 13)
(13, 'S', 'BLACK_WHITE', 20),
(13, 'M', 'BLACK_WHITE', 30),
(13, 'L', 'BLACK_WHITE', 25),
(13, 'XL', 'BLACK_WHITE', 18),

-- 디젤 T 앤지 티셔츠 (product_id: 14)
(14, 'S', 'WHITE', 3),
(14, 'M', 'WHITE', 5),
(14, 'L', 'WHITE', 4),
(14, 'XL', 'WHITE', 2);


-- 하의 카테고리 상품 데이터 추가
INSERT INTO product (category_id, product_name, img1, img2, img3, img4, img5, content, price, sale, brand, gender, deleted, wishlist_count) VALUES
-- 하의 상품들 (category_id: 3)
(3, '와이드 데님 팬츠 - 라이트 블루', 'https://image.msscdn.net/thumbnails/images/goods_img/20240301/3901234/3901234_17091234567890_big.jpg?w=780', 'https://image.msscdn.net/thumbnails/images/goods_img/20240301/3901234/3901234_17091234567890_big.jpg?w=1200', null, null, null, '편안한 와이드 핏의 데님 팬츠입니다.', 89000, 79000, '유니클로', 'UNISEX', false, 0),

(3, '슬림 블랙 진 - 스키니 핏', 'https://image.msscdn.net/thumbnails/images/goods_img/20240215/3801234/3801234_17081234567890_big.jpg?w=780', 'https://image.msscdn.net/thumbnails/images/goods_img/20240215/3801234/3801234_17081234567890_big.jpg?w=1200', null, null, null, '슬림한 핏의 블랙 진입니다.', 129000, 99000, '리바이스', 'UNISEX', false, 0),

(3, '카고 팬츠 - 베이지', 'https://image.msscdn.net/thumbnails/images/goods_img/20240320/3951234/3951234_17101234567890_big.jpg?w=780', 'https://image.msscdn.net/thumbnails/images/goods_img/20240320/3951234/3951234_17101234567890_big.jpg?w=1200', null, null, null, '실용적인 카고 팬츠입니다.', 69000, 0, '스트릿웨어', 'UNISEX', false, 0),

(3, '와이드 슬랙스 - 네이비', 'https://image.msscdn.net/thumbnails/images/goods_img/20240410/4001234/4001234_17111234567890_big.jpg?w=780', 'https://image.msscdn.net/thumbnails/images/goods_img/20240410/4001234/4001234_17111234567890_big.jpg?w=1200', null, null, null, '포멀한 와이드 슬랙스입니다.', 159000, 139000, '지오다노', 'UNISEX', false, 0),

(3, '조거 팬츠 - 그레이', 'https://image.msscdn.net/thumbnails/images/goods_img/20240505/4101234/4101234_17121234567890_big.jpg?w=780', 'https://image.msscdn.net/thumbnails/images/goods_img/20240505/4101234/4101234_17121234567890_big.jpg?w=1200', null, null, null, '편안한 조거 팬츠입니다.', 45000, 0, '나이키', 'UNISEX', false, 0);
-- 하의 상품 변형 데이터 (ProductVariant)
INSERT INTO product_variant (product_id, size, color, quantity) VALUES
-- 와이드 데님 팬츠 (product_id: 15)
(15, 'S', 'LIGHT_BLUE', 12),
(15, 'M', 'LIGHT_BLUE', 18),
(15, 'L', 'LIGHT_BLUE', 15),
(15, 'XL', 'LIGHT_BLUE', 10),

-- 슬림 블랙 진 (product_id: 16)
(16, 'S', 'BLACK', 15),
(16, 'M', 'BLACK', 22),
(16, 'L', 'BLACK', 18),
(16, 'XL', 'BLACK', 12),

-- 카고 팬츠 (product_id: 17)
(17, 'S', 'BEIGE', 10),
(17, 'M', 'BEIGE', 16),
(17, 'L', 'BEIGE', 14),
(17, 'XL', 'BEIGE', 8),

-- 와이드 슬랙스 (product_id: 18)
(18, 'S', 'NAVY', 8),
(18, 'M', 'NAVY', 12),
(18, 'L', 'NAVY', 10),
(18, 'XL', 'NAVY', 6),

-- 조거 팬츠 (product_id: 19)
(19, 'S', 'GRAY', 20),
(19, 'M', 'GRAY', 25),
(19, 'L', 'GRAY', 22),
(19, 'XL', 'GRAY', 18);

-- 아바타 데이터 (Avatar)
---- 테스트용 아바타들 - 다양한 스타일과 조합으로 구성
--INSERT INTO avatar (user_id, pose_img, upper_mask_img, lower_mask_img, avatar_img, is_bookmarked) VALUES
------ user_id 1 (테스트사용자)의 아바타들
--(4, 'https://example.com/pose/pose1.png', 'https://example.com/mask/upper1.png', 'https://example.com/mask/lower1.png', 'https://i.ibb.co/qYj9VdST/ex10.png', false),
--(4, 'https://example.com/pose/pose2.png', 'https://example.com/mask/upper2.png', 'https://example.com/mask/lower2.png', 'https://i.ibb.co/qYj9VdST/ex10.png', true),
--(4, 'https://example.com/pose/pose3.png', 'https://example.com/mask/upper3.png', 'https://example.com/mask/lower3.png', 'https://i.ibb.co/qYj9VdST/ex10.png', false),
--(4, 'https://example.com/pose/pose4.png', 'https://example.com/mask/upper4.png', 'https://example.com/mask/lower4.png', 'https://i.ibb.co/qYj9VdST/ex10.png', false),
--(4, 'https://example.com/pose/pose5.png', 'https://example.com/mask/upper5.png', 'https://example.com/mask/lower5.png', 'https://i.ibb.co/qYj9VdST/ex10.png', true),
--(4, 'https://example.com/pose/pose6.png', 'https://example.com/mask/upper6.png', 'https://example.com/mask/lower6.png', 'https://i.ibb.co/qYj9VdST/ex10.png', false),
--(4, 'https://example.com/pose/pose7.png', 'https://example.com/mask/upper7.png', 'https://example.com/mask/lower7.png', 'https://i.ibb.co/qYj9VdST/ex10.png', false),
--(4, 'https://example.com/pose/pose8.png', 'https://example.com/mask/upper8.png', 'https://example.com/mask/lower8.png', 'https://i.ibb.co/qYj9VdST/ex10.png', true),
--(4, 'https://example.com/pose/pose9.png', 'https://example.com/mask/upper9.png', 'https://example.com/mask/lower9.png', 'https://i.ibb.co/qYj9VdST/ex10.png', false),
--(4, 'https://example.com/pose/pose10.png', 'https://example.com/mask/upper10.png', 'https://example.com/mask/lower10.png', 'https://i.ibb.co/qYj9VdST/ex10.png', true);
--
---- 아바타 아이템 데이터 (AvatarItem)
---- 각 아바타가 착용하고 있는 상품들을 연결
--INSERT INTO avatar_item (avatar_id, product_id) VALUES
---- 아바타 1: 포우하트키 반팔티 + 기본 하의
--(1, 1),  -- 포우하트키 반팔티
--
---- 아바타 2: STRANGE PUPPY TEE + 기본 하의
--(2, 2),  -- STRANGE PUPPY TEE
--
---- 아바타 3: 2PACK 티셔츠
--(3, 3),  -- 2PACK 티셔츠
--
---- 아바타 4: 버버리 모노그램 티셔츠
--(4, 4),  -- 버버리 모노그램 티셔츠
--
---- 아바타 5: 플루크 어드벤쳐 티셔츠
--(5, 5),  -- 플루크 어드벤쳐 티셔츠
--
---- 아바타 6: 스파오 가나디 티셔츠
--(6, 6),  -- 스파오 가나디 티셔츠
--
---- 아바타 7: 미니멀프로젝트 플라워 티셔츠
--(7, 7),  -- 미니멀프로젝트 플라워 티셔츠
--
---- 아바타 8: 앨빈클로 MANIFEST 티셔츠
--(8, 8),  -- 앨빈클로 MANIFEST 티셔츠
--
---- 아바타 9: 유센틱 CUPID 티셔츠
--(9, 9),  -- 유센틱 CUPID 티셔츠
--
---- 아바타 10: 웬즈데이오아시스 빈티지 스트라이프 티셔츠
--(10, 10); -- 웬즈데이오아시스 빈티지 스트라이프 티셔츠

-- 아바타에 하의 아이템 추가 (완전한 코디 구성)
--INSERT INTO avatar_item (avatar_id, product_id) VALUES
---- 각 아바타에 하의 추가
--(1, 15),  -- 아바타 1: 와이드 데님 팬츠
--(2, 16),  -- 아바타 2: 슬림 블랙 진
--(3, 17),  -- 아바타 3: 카고 팬츠
--(4, 18),  -- 아바타 4: 와이드 슬랙스
--(5, 19),  -- 아바타 5: 조거 팬츠
--(6, 15),  -- 아바타 6: 와이드 데님 팬츠
--(7, 16),  -- 아바타 7: 슬림 블랙 진
--(8, 17),  -- 아바타 8: 카고 팬츠
--(9, 18),  -- 아바타 9: 와이드 슬랙스
--(10, 19); -- 아바타 10: 조거 팬츠

-- 스토리 테스트 데이터 (Story)
-- 아바타를 활용한 스토리들 - MySQL 호환
--INSERT INTO story (avatar_id, story_image_url, contents, like_count, author_id) VALUES
--(1, '/images/dummy/ex10.png', '오늘의 OOTD! 포우하트키 반팔티와 와이드 데님으로 캐주얼하게 코디했어요 ✨ #데일리룩 #캐주얼', 15, 1),
--(2, '/images/dummy/ex11.png', 'STRANGE PUPPY TEE로 포인트를 준 스트릿 룩! 심플하지만 개성있는 스타일 🐶 #스트릿패션 #개성', 23, 1),
--(3, '/images/dummy/ex12.png', '2팩 티셔츠의 활용법! 카고팬츠와 매치해서 편안하면서도 스타일리시하게 👕 #실용적 #편안함', 8, 1),
--(4, '/images/dummy/ex13.png', '버버리 모노그램으로 럭셔리한 느낌을 연출했어요. 슬랙스와의 조합이 완벽! 💎 #럭셔리 #포멀', 31, 1),
--(5, '/images/dummy/ex14.png', '플루크 어드벤쳐 티셔츠로 활동적인 룩 완성! 조거팬츠와 찰떡궁합 🏃‍♂️ #액티브 #스포츠', 12, 1),
--(6, '/images/dummy/ex15.png', '스파오 가나디 티셔츠의 깔끔한 매력! 데님과 함께 기본에 충실한 코디 👌 #베이직 #깔끔', 19, 1),
--(7, '/images/dummy/ex16.png', '미니멀프로젝트의 플라워 프린팅이 포인트! 네이비 컬러가 세련되네요 🌸 #미니멀 #세련', 27, 1),
--(8, '/images/dummy/ex17.png', '앨빈클로 MANIFEST로 트렌디한 룩! 카고팬츠와의 조합이 힙해요 🔥 #트렌디 #힙', 35, 1),
--(9, '/images/dummy/ex18.png', '유센틱 CUPID 티셔츠의 빈티지한 매력! 슬랙스로 밸런스를 맞췄어요 💕 #빈티지 #밸런스', 22, 1),
--(10, '/images/dummy/ex19.png', '웬즈데이오아시스 스트라이프로 레트로 감성! 조거팬츠로 편안함까지 더했어요 📸 #레트로 #편안', 16, 1);
