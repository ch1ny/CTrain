<template>
	<div>
		<div>
			<el-table :data="friends" border stripe>
				<el-table-column prop="id" label="身份证号"></el-table-column>
				<el-table-column prop="name" label="乘客姓名"></el-table-column>
				<el-table-column label="操作" width="100">
					<template slot-scope="scope">
						<el-button type="danger" @click="del(scope.$index)">移除</el-button>
					</template>
				</el-table-column>
				<div slot="append">
					<el-button plain style="width: 100%; margin-bottom: 1px" @click="isAdding=true;"><i class="el-icon-plus"></i></el-button>
				</div>
			</el-table>
		</div>

		<div class="others">
			<el-dialog title="添加同行乘客" :visible.sync="isAdding" @open="$refs.addForm?$refs.addForm.resetFields():''">
				<el-form :model="addForm" ref="addForm" :rules="addRules">
					<el-form-item prop="id">
						<el-input v-model="addForm.id" placeholder="请输入同行人员的身份证号码" prefix-icon="iconfont icon-credentials_icon"></el-input>
					</el-form-item>
					<el-form-item prop="name">
						<el-input v-model="addForm.name" placeholder="请输入同行人员的姓名" prefix-icon="el-icon-user"></el-input>
					</el-form-item>
				</el-form>
				<el-button type="primary" style="width: 15%" @click="add">添&nbsp;&nbsp;&nbsp;加</el-button>
			</el-dialog>
		</div>
	</div>
</template>

<script>
import $ajax from "../ajax";

export default {
	data() {
		return {
			friends: [],
			isAdding: false,
			addForm: {
				id: "",
				name: "",
			},
			addRules: {
				id: [
					{
						required: true,
						message: "请输入同行人员的身份证号码",
						trigger: ["blur", "change"],
					},
				],
				name: [
					{
						required: true,
						message: "请输入同行人员的姓名",
						trigger: ["blur", "change"],
					},
				],
			},
		};
	},
	methods: {
		async getFriends() {
			let res = await $ajax.post(
				"/friend/get",
				{},
				{
					headers: {
						Authorization:
							"Bearer " + localStorage.getItem("token"),
					},
				}
			);
			this.friends = res.data.data;
		},
		add() {
			this.$refs.addForm.validate(async (valid) => {
				if (valid) {
					let res = await $ajax.post(
						"/friend/add",
						{
							id: this.addForm.id,
							name: this.addForm.name,
						},
						{
							headers: {
								Authorization:
									"Bearer " + localStorage.getItem("token"),
							},
						}
					);
					if (res.data.code == 0) {
						let json = JSON.parse(JSON.stringify(this.addForm));
						this.friends.push(json);
						this.$message({
							message: "添加成功！",
							type: "success",
							duration: 1000,
						});
						this.isAdding = false;
					} else {
						this.$message({
							message: `添加失败！${res.data.data}`,
							type: "error",
							duration: 1000,
						});
					}
				}
			});
		},
		async del(index) {
			let res = await $ajax.post(
				"/friend/del",
				{
					id: this.friends[index].id,
				},
				{
					headers: {
						Authorization: localStorage.getItem("token"),
					},
				}
			);
			if (res.data.code == 0) {
				this.$message({
					message: "移除成功！",
					type: "success",
					duration: 1000,
				});
				this.friends.splice(index, 1);
			} else {
				this.$message({
					message: `移除失败！${res.data.data}`,
					type: "error",
					duration: 1000,
				});
			}
		},
	},
	created() {
		this.getFriends();
	},
};
</script>