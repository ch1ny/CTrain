<template>
	<div>

		<el-container>
			<el-header style="background-color: white; height: 50px">
				<el-page-header style="line-height: 50px" content="回到上一页" @back="$router.go(-1)"></el-page-header>
			</el-header>
			<el-main style="padding-top: 0px">
				<el-card class="main">
					<div slot="header" style="text-align: left">
						<span><b>个人信息</b></span>
					</div>
					<div class="container" style="text-align: left">
						<div class="avatar" style="display: flex;   ">
							<el-tooltip class="item" effect="dark" placement="right-end">
								<div slot="content">为了保障您的使用体验，<br />请尽可能上传7M以下的图片</div>
								<el-upload class="avatar-uploader" action="" :show-file-list="false" :on-change="imgCropper" :auto-upload="false" ref="upload">
									<el-image style="width: 10vw; height: 10vw; float: left" :src="url"></el-image>
								</el-upload>
							</el-tooltip>
							<div style="width: 10vw; height: 10vw; background: rgba(150, 150, 150, 0.7); position: absolute" v-if="isUpdatingAvatar">
								<i class="el-icon-loading" style="position: relative; left: 3vw; top: 3vw; font-size: 4vw; color: white"></i>
							</div>
							<div style="margin-left: 50px">
								<div>
									<el-button type="info" @click="defaultAvatar">恢复默认头像</el-button>
								</div>
								<div style="margin-top: 10px">
									<el-button type="warning" @click="isUpdatingPsw=true;">修改密码</el-button>
								</div>
							</div>
						</div>
						<el-divider content-position="left"><b>基本信息</b></el-divider>
						<div class="baseInfo">
							<el-form>
								<el-form-item label="用户姓名：">
									<span class="infos">{{myInfo.name}}</span>
								</el-form-item>
								<el-form-item label="身份证号：">
									<span class="infos">{{myInfo.id}}</span>
								</el-form-item>
								<el-form-item label="注册邮箱：">
									<span class="infos">{{myInfo.email}}</span>
								</el-form-item>
							</el-form>
						</div>
						<el-divider content-position="left"><b>同行乘客</b></el-divider>
						<Friends></Friends>
					</div>
				</el-card>
			</el-main>
		</el-container>

		<div class="others">

			<el-dialog title="修改密码" :visible.sync="isUpdatingPsw" @open="$refs.updateForm?$refs.updateForm.resetFields():''">
				<el-form :model="updateForm" ref="updateForm" :rules="updateRules">
					<el-form-item label="旧密码" prop="oldPsw">
						<el-input type="password" @keydown.enter.native="updatePsw" v-model="updateForm.oldPsw" placeholder="请输入原来的密码" prefix-icon="el-icon-key"></el-input>
					</el-form-item>
					<el-form-item label="新密码" prop="newPsw">
						<el-input type="password" @keydown.enter.native="updatePsw" v-model="updateForm.newPsw" placeholder="请输入新的密码" prefix-icon="el-icon-key"></el-input>
					</el-form-item>
				</el-form>
				<el-button @click="updatePsw" type="primary">更新密码</el-button>
			</el-dialog>

			<div class="out" v-if="cropping" style="position: fixed; left: 0%; top: 0%; background: rgba(50, 50, 50, 0.7); width: 100%; height: 100%;">
				<div class="in" style="position: relative; top: 5vh; width: 80vh; height: 80vh; margin: 0 auto;">
					<vue-cropper autoCrop :img="pic" ref="cropper" centerBox :outputSize="1" :infoTrue="true" :outputType="'png'" fixed :fixedNumber="[1,1]" />
					<el-button type="danger" style="margin-top: 25px; margin-inline: 20px" @click="$refs.upload.clearFiles();cropping=false;pic=''">取消裁剪</el-button>
					<el-button type="success" style="margin-top: 25px; margin-inline: 20px" @click="setAvatar">裁剪完了</el-button>
				</div>
			</div>

		</div>
	</div>
</template>

<script>
import $ajax from "../ajax";
import Friends from "../components/Friends.vue";

export default {
	components: {
		Friends,
	},
	data() {
		return {
			url: "",
			pic: "",
			cropping: false,
			myInfo: {},
			isUpdatingAvatar: false,
			isUpdatingPsw: false,
			updateForm: {
				oldPsw: "",
				newPsw: "",
			},
			updateRules: {
				oldPsw: [
					{
						required: true,
						message: "旧密码不得为空",
						trigger: ["blur", "change"],
					},
				],
				newPsw: [
					{
						required: true,
						message: "新密码不得为空",
						trigger: ["blur", "change"],
					},
				],
			},
		};
	},
	methods: {
		initInfo() {
			let jwt = require("jsonwebtoken");
			let token = jwt.decode(localStorage.getItem("token"));
			this.myInfo.id = token.sub;
			this.myInfo.email = token.email;
			this.myInfo.name = token.aud;
			if (localStorage.getItem("avatar") == null) {
				let sexCode = Number(
					this.myInfo.id.substr(this.myInfo.id.length - 2, 1)
				);
				if (sexCode % 2 == 0) {
					this.url = require("@/assets/icon/female.png");
				} else {
					this.url = require("@/assets/icon/male.png");
				}
			} else {
				this.url = localStorage.getItem("avatar");
				this.getCompleteAvatar();
			}
		},
		async getCompleteAvatar() {
			let res = await $ajax.post(
				"/user/getAvatar",
				{},
				{
					headers: {
						Authorization:
							"Bearer " + localStorage.getItem("token"),
					},
				}
			);
			if (res.data.code == 0 && res.data.data != "") {
				this.url = res.data.data;
			}
		},
		imgCropper(file) {
			const isJPG =
				file.raw.type === "image/jpeg" || file.raw.type === "image/png";
			const isLt4M = file.raw.size / 1024 / 1024 < 10;
			if (!isJPG) {
				this.$message.error("上传头像图片只能是 JPG 或 PNG 格式!");
				return;
			}
			if (!isLt4M) {
				this.$message.error("上传头像图片大小不能超过 10MB!");
				return;
			}
			let reader = new FileReader();
			reader.readAsDataURL(file.raw);
			reader.onload = () => {
				this.pic = reader.result;
				this.cropping = true;
			};
		},
		setAvatar() {
			this.cropping = false;
			this.$refs.cropper.getCropBlob((data) => {
				let reader = new FileReader();
				reader.readAsDataURL(data);
				reader.onload = async () => {
					this.isUpdatingAvatar = true;
					let startTime = new Date().getTime();
					let res = await $ajax.post(
						"/user/setAvatar",
						{
							avatar: reader.result,
						},
						{
							headers: {
								Authorization:
									"Bearer " + localStorage.getItem("token"),
							},
						}
					);
					let endTime = new Date().getTime();
					let time = endTime - startTime;
					if (res.data.code == 0) {
						if (time >= 2500) {
							this.$message({
								message: `上传头像花费约${
									time / 1000
								}秒，头像图片过大可能会影响您的使用体验，建议您上传更小的图片`,
								type: "warning",
								showClose: true,
								duration: 0,
							});
						} else {
							this.$message.success("头像修改成功！");
						}
						this.url = reader.result;
						this.isUpdatingAvatar = false;
						res = await $ajax.post(
							"/user/getThumbnailAvatar",
							{},
							{
								headers: {
									Authorization:
										"Bearer " +
										localStorage.getItem("token"),
								},
							}
						);
						if (res.data.code == 0 && res.data.data != "") {
							localStorage.setItem("avatar", res.data.data);
						}
					} else {
						this.isUpdatingAvatar = false;
						this.$message.error("头像上传失败！");
					}
					this.$refs.upload.clearFiles();
				};
			});
		},
		updatePsw() {
			this.$refs.updateForm.validate(async (valid) => {
				if (valid) {
					let res = await $ajax.post(
						"/user/updatePsw",
						this.updateForm,
						{
							headers: {
								Authorization:
									"Bearer " + localStorage.getItem("token"),
							},
						}
					);
					if (res.data.code == 0) {
						localStorage.removeItem("token");
						localStorage.removeItem("avatar");
						this.$message({
							message: "密码修改成功，请重新登陆",
							type: "success",
							duration: 1000,
							onClose: () => {
								this.$router.push({ path: "/login" });
							},
						});
					} else {
						this.$message({
							message: `密码修改失败，${res.data.data}`,
							type: "error",
							duration: 1000,
						});
					}
				}
			});
		},
		async defaultAvatar() {
			await $ajax.post(
				"user/defaultAvatar",
				{},
				{
					headers: {
						Authorization:
							"Bearer " + localStorage.getItem("token"),
					},
				}
			);
			localStorage.removeItem("avatar");
			let sexCode = Number(
				this.myInfo.id.substr(this.myInfo.id.length - 2, 1)
			);
			if (sexCode % 2 == 0) {
				this.url = require("@/assets/icon/female.png");
			} else {
				this.url = require("@/assets/icon/male.png");
			}
		},
	},
	created() {
		if (localStorage.getItem("token") == null) {
			this.$router.replace("/");
		}
		this.initInfo();
	},
};
</script>

<style scoped>
.infos {
	font-size: 20px;
}
</style>