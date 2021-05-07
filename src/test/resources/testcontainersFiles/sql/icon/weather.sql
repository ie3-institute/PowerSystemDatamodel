CREATE TABLE public.weather
(
    time timestamp with time zone,
    alb_rad double precision,
    asob_s double precision,
    aswdifd_s double precision,
    aswdifu_s double precision,
    aswdir_s double precision,
    t_2m double precision,
    t_g double precision,
    u_10m double precision,
    u_131m double precision,
    u_20m double precision,
    u_216m double precision,
    u_65m double precision,
    v_10m double precision,
    v_131m double precision,
    v_20m double precision,
    v_216m double precision,
    v_65m double precision,
    w_131m double precision,
    w_20m double precision,
    w_216m double precision,
    w_65m double precision,
    z0 double precision,
    coordinate_id integer,
    p_131m double precision,
    p_20m double precision,
    p_65m double precision,
    sobs_rad double precision,
    t_131m double precision,
    CONSTRAINT weather_pkey PRIMARY KEY (time, coordinate_id),
    CONSTRAINT weather_time_coordinate_unique UNIQUE (time, coordinate_id)
)
    WITH (
        OIDS = FALSE
    )
    TABLESPACE pg_default;

CREATE INDEX weather_coordinate_idx
    ON public.weather USING btree
        (coordinate_id ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE INDEX weather_coordinate_time_idx
    ON public.weather USING btree
        (coordinate_id ASC NULLS LAST, time ASC NULLS LAST)
    TABLESPACE pg_default;

INSERT INTO
    public.weather (time, alb_rad, asob_s, aswdifd_s, aswdifu_s, aswdir_s, t_2m, t_g, u_10m, u_131m, u_20m, u_216m, u_65m, v_10m, v_131m, v_20m, v_216m, v_65m, w_131m, w_20m, w_216m, w_65m, z0, coordinate_id, p_131m, p_20m, p_65m, sobs_rad, t_131m)
VALUES
('2019-08-01 15:00:00+0', 13.015240669, 503.469742643732, 228.021339757131, 80.8246124780934, 356.2648859375, 297.624199265982, 300.6632065669, 2.59460377536322, 3.76589711568313, 2.5812495613105, 3.94152121323647, 3.4740205817325, -0.0240786467212414, -0.0297608319165961, -0.0529678853045105, -0.00969812551875571, -0.0499661079932472, 0.00409144377409565, 0.0015809058504647, 0.00595448465750138, 0.00266634369620467, 0.955322166563199, 67775, NULL, NULL, NULL, NULL, NULL),
('2019-08-01 16:00:00+0', 13.015240669, 348.844393096138, 200.46049098038, 56.004364311073, 204.38963365625, 297.320002347335, 298.844773762216, 2.55725934195279, 4.01651967392677, 2.55434171324423, 4.20461049739088, 3.67091211581564, -0.384635762595304, -0.574806421919763, -0.400129700426715, -0.574231301551345, -0.548460101273113, 0.00842078158830364, 0.00402891995554883, 0.0103738560877878, 0.00642120845009564, 0.955323652611887, 67775, NULL, NULL, NULL, NULL, NULL),
('2019-08-01 17:00:00+0', 13.015240669, 306.571394509505, 180.734296104002, 49.1986036554934, 175.039569078125, 296.828740358407, 297.659601745757, 2.25171266161903, 3.65066950489564, 2.2438962003705, 3.79807360304293, 3.33915291121142, -0.69508935296193, -1.10853234501432, -0.712247865350599, -1.12930575743682, -1.03523090092579, 0.0124649216553269, 0.00596557511757611, 0.0152653602980477, 0.00963211312941292, 0.955322760336952, 67775, NULL, NULL, NULL, NULL, NULL),
('2019-08-01 15:00:00+0', 13.013334274, 498.219742300774, 245.240790378413, 80.0782271098217, 333.0547140625, 295.515335568404, 297.436843518738, 2.69074813301161, 4.00160121993898, 2.68883329948458, 4.14046943742341, 3.71403226367276, 1.20973866598336, 1.81482331766854, 1.19637364179174, 1.89445925143369, 1.66706360898832, -0.0107351344598088, -0.0063504912633166, -0.012234044440805, -0.00904490847631571, 0.955336762972383, 67776, NULL, NULL, NULL, NULL, NULL),
('2019-08-01 16:00:00+0', 13.013334274, 287.163521177577, 241.641483540946, 46.1826228914206, 91.7093913229698, 293.455111314491, 294.987683227439, 2.24292571281681, 3.28865545990928, 2.24693045663629, 3.45619617779588, 3.06350529841655, 0.705285607539457, 1.01736584328251, 0.69403099565213, 1.08316452397628, 0.940270018404624, -0.00960905133118967, -0.00372074627807391, -0.0126429015220642, -0.00643946343215643, 0.955336572337004, 67776, NULL, NULL, NULL, NULL, NULL);
