#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
生成微信小程序图标
"""
from PIL import Image, ImageDraw

# 颜色定义
COLOR_GRAY = (153, 153, 153)      # #999 - 未选中
COLOR_BLUE = (24, 144, 255)       # #1890FF - 选中
COLOR_WHITE = (255, 255, 255)     # 白色
COLOR_LIGHT_GRAY = (240, 240, 240)  # 浅灰背景

def draw_home_icon(color, size=81):
    """首页图标 - 房子"""
    img = Image.new('RGB', (size, size), COLOR_WHITE)
    draw = ImageDraw.Draw(img)

    padding = size // 6
    center_x = size // 2
    roof_top = padding
    roof_bottom = size // 2
    house_left = padding
    house_right = size - padding
    house_bottom = size - padding

    # 画屋顶（三角形）
    roof_points = [
        (center_x, roof_top),
        (house_left, roof_bottom),
        (center_x, roof_bottom),
        (house_right, roof_bottom)
    ]
    draw.polygon(roof_points, fill=color)

    # 画房子主体（矩形）
    door_width = size // 5
    door_height = size // 3
    draw.rectangle(
        [center_x - door_width // 2, roof_bottom,
         center_x + door_width // 2, roof_bottom + door_height],
        fill=COLOR_WHITE
    )

    return img

def draw_order_icon(color, size=81):
    """订单图标 - 文档"""
    img = Image.new('RGB', (size, size), COLOR_WHITE)
    draw = ImageDraw.Draw(img)

    padding = size // 5
    inner_padding = size // 12

    # 画文档外框
    draw.rectangle(
        [padding, padding * 1.5, size - padding, size - padding],
        outline=color,
        width=size // 16
    )

    # 画文档内的线条
    line_y_start = padding * 2.5
    line_spacing = size // 10
    line_width = size - padding * 2.5

    for i in range(3):
        line_y = line_y_start + i * line_spacing
        draw.line(
            [(padding * 1.8, line_y), (padding * 1.8 + line_width, line_y)],
            fill=color,
            width=size // 20
        )

    return img

def draw_mine_icon(color, size=81):
    """我的图标 - 人形"""
    img = Image.new('RGB', (size, size), COLOR_WHITE)
    draw = ImageDraw.Draw(img)

    center_x = size // 2

    # 画头部（圆形）
    head_radius = size // 6
    head_center_y = size // 3
    draw.ellipse(
        [center_x - head_radius, head_center_y - head_radius,
         center_x + head_radius, head_center_y + head_radius],
        fill=color
    )

    # 画身体（半椭圆）
    body_top = head_center_y + head_radius
    body_bottom = size - size // 6
    draw.arc(
        [center_x - head_radius * 2.5, body_top,
         center_x + head_radius * 2.5, body_bottom * 2 - body_top],
        0, 180,
        fill=color,
        width=head_radius * 2
    )

    return img

def draw_default_avatar(size=200):
    """默认头像"""
    img = Image.new('RGB', (size, size), (240, 240, 240))
    draw = ImageDraw.Draw(img)

    center_x = size // 2

    # 画头部（圆形）
    head_radius = size // 5
    head_center_y = size // 3
    draw.ellipse(
        [center_x - head_radius, head_center_y - head_radius,
         center_x + head_radius, head_center_y + head_radius],
        fill=(200, 200, 200)
    )

    # 画身体
    body_top = head_center_y + head_radius
    body_bottom = size - size // 8
    draw.arc(
        [center_x - head_radius * 2.5, body_top,
         center_x + head_radius * 2.5, body_bottom * 2 - body_top],
        0, 180,
        fill=(200, 200, 200),
        width=head_radius * 2
    )

    return img

def draw_default_coupon(size=200):
    """默认洗车券图片"""
    img = Image.new('RGB', (size, size), (245, 245, 245))
    draw = ImageDraw.Draw(img)

    padding = size // 5

    # 画券的外框
    draw.rectangle(
        [padding, padding, size - padding, size - padding],
        fill=(220, 220, 220),
        outline=(180, 180, 180),
        width=size // 40
    )

    # 画券内的装饰线条
    inner_padding = padding + size // 10
    for i in range(3):
        line_y = inner_padding + i * (size // 8)
        draw.line(
            [(inner_padding, line_y), (size - inner_padding, line_y)],
            fill=(200, 200, 200),
            width=size // 30
        )

    return img

def save_icon(icon_func, filename, color=None, size=81):
    """生成并保存图标"""
    if color:
        img = icon_func(color, size)
    else:
        img = icon_func(size)
    img.save(filename, 'PNG')
    print(f"已生成：{filename}")

if __name__ == '__main__':
    import os

    # 确保 images 目录存在
    script_dir = os.path.dirname(os.path.abspath(__file__))
    images_dir = os.path.join(script_dir, 'cardwash-miniprogram', 'images')
    os.makedirs(images_dir, exist_ok=True)

    print("开始生成图标...")

    # 生成 TabBar 图标（81x81）
    save_icon(draw_home_icon, os.path.join(images_dir, 'home.png'), COLOR_GRAY, 81)
    save_icon(draw_home_icon, os.path.join(images_dir, 'home-active.png'), COLOR_BLUE, 81)
    save_icon(draw_order_icon, os.path.join(images_dir, 'order.png'), COLOR_GRAY, 81)
    save_icon(draw_order_icon, os.path.join(images_dir, 'order-active.png'), COLOR_BLUE, 81)
    save_icon(draw_mine_icon, os.path.join(images_dir, 'mine.png'), COLOR_GRAY, 81)
    save_icon(draw_mine_icon, os.path.join(images_dir, 'mine-active.png'), COLOR_BLUE, 81)

    # 生成默认图片（200x200）
    save_icon(draw_default_avatar, os.path.join(images_dir, 'default-avatar.png'), None, 200)
    save_icon(draw_default_coupon, os.path.join(images_dir, 'default-coupon.png'), None, 200)

    print("图标生成完成！")
