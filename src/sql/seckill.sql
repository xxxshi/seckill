create database seckill;

use seckill;

create table seckill(
`seckill_id` bigint not null auto_increment comment '商品库存id',
`name` varchar(120) not null comment '商品名称',
`number` int not null comment '库存数量',
`start_time` timestamp not null comment '商品秒杀开启时间',
`end_time` timestamp not null comment '商品秒杀接受时间',
`create_time` timestamp not null DEFAULT CURRENT_TIMESTAMP comment '创建时间',
primary key(seckill_id),
key idx_start_time(start_time),
key idx_end_time(end_time),
key idx_create_time(create_time)
)engine=InnoDB AUTO_INCREMENT=1000 DEFAULT charset=utf8 comment '秒杀库存表';

insert into
	seckill(name,number, start_time,end_time)
values
	('1000元秒杀iphone7',100,'2018-9-11 00:00:00','2018-9-12 00:00:00'),
	('800元秒杀ipad3',200,'2018-9-11 00:00:00','2018-9-12 00:00:00'),
	('500元秒杀xiaomi8',300,'2018-9-11 00:00:00','2018-9-12 00:00:00'),
	('300元秒杀红米note',400,'2018-9-11 00:00:00','2018-9-12 00:00:00');

create table success_killed(
`seckill_id` bigint not null comment '商品库存id',
`user_phone` bitint not null comment '用户手机号码',
`state` bitint not null DEFAULT -1 comment'状态 -1无效 0成功 1已成功 2已发货',
`create_time` timestamp not null DEFAULT CURRENT_TIMESTAMP comment '创建时间',
PRIMARY key(seckill_id,user_phone),
key idx_create_time(create_time)
)engine=InnoDB  DEFAULT charset=utf8 comment '秒杀成功明细表';